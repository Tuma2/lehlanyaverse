package fyi.lehlanyaverse.components.fx

import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** Minimal binding: ResizeObserver isn't part of Kotlin/JS's bundled DOM API. */
private external class ResizeObserver(callback: () -> Unit) {
    fun observe(target: Element)
}

/** One scroll position, handed to every frame callback. */
class ScrollFrame(val y: Double, val progress: Double, val viewportW: Double, val viewportH: Double)

/**
 * The single owner of page scrolling. Wraps Lenis (smooth wheel, heavy lerp, native touch) and runs the one
 * requestAnimationFrame loop that drives every continuous scroll effect, so nothing else reads scroll on its own.
 *
 * Without motion (reduced-motion preference, [Fx.SMOOTH_SCROLL] off, or a touch-only device, which scrolls
 * natively anyway) there is no Lenis: a passive native scroll listener feeds the same callbacks.
 */
object SmoothScroll {
    private var lenis: Lenis? = null
    private var started = false
    private var locks = 0

    private val frameCallbacks = mutableListOf<(ScrollFrame) -> Unit>()
    private val measureCallbacks = mutableListOf<() -> Unit>()
    private var frameRequests = mutableListOf<(Double) -> Unit>()

    // Layout values, cached so frames do arithmetic only. Refreshed whenever the page or viewport resizes.
    private var maxScroll = 1.0
    private var viewportW = 0.0
    private var viewportH = 0.0

    private var lastY = Double.NaN
    private var dirty = true
    private var tickQueued = false

    /** Called once, after the app has mounted over the pre-rendered page (PageLayout). */
    fun init() {
        if (started || AppGlobals.isExporting) return
        started = true

        measure()
        ResizeObserver { measure() }.observe(document.body!!)
        window.addEventListener("resize", { measure() })
        document.addEventListener("click", ::onClick)

        // Touch-only devices scroll natively either way, so Lenis would only add a per-frame loop there
        if (Fx.SMOOTH_SCROLL && Fx.motion && !Fx.touchOnly) {
            lenis = Lenis(jsObject {
                lerp = 0.09            // weighty: the page keeps travelling a beat after the wheel stops
                wheelMultiplier = 0.9
                smoothWheel = true
                syncTouch = false      // touch scrolling stays native
                autoRaf = false        // driven by the loop below, together with the effects
            })
            holdDuringBootIntro()
            window.requestAnimationFrame(::loop)
        } else {
            window.addEventListener("scroll", { requestTick() }, jsObject<dynamic> { passive = true })
            requestTick()
        }
    }

    /** Runs [cb] on every frame the scroll position changes. Returns a disposer. */
    fun onFrame(cb: (ScrollFrame) -> Unit): () -> Unit {
        frameCallbacks += cb
        dirty = true
        requestTick()
        return { frameCallbacks -= cb }
    }

    /**
     * Runs [cb] once, on the next frame of the shared loop, after the scroll callbacks (so it sees this frame's
     * scroll position). For effects that animate on their own, like the cursor reticle: request again to continue.
     */
    fun requestFrame(cb: (time: Double) -> Unit) {
        frameRequests += cb
        requestTick()
    }

    /** Runs [cb] (layout reads only, no writes) whenever the page or viewport resizes. Returns a disposer. */
    fun onMeasure(cb: () -> Unit): () -> Unit {
        measureCallbacks += cb
        cb()
        return { measureCallbacks -= cb }
    }

    /** Pauses smooth scrolling (boot intro, modals). Ref-counted: every lock needs one [unlock]. */
    fun lock() {
        if (locks++ == 0) lenis?.stop()
    }

    fun unlock() {
        if (locks > 0 && --locks == 0) lenis?.start()
    }

    /**
     * Glides [target] to just below the sticky header, then moves focus there for keyboard and screen readers.
     * The header offset comes from `scroll-padding-top` in hud.css, which both Lenis and native scrolling honour,
     * so the two paths always land in the same place.
     */
    fun scrollToTarget(target: HTMLElement) {
        val arrive = {
            if (!target.hasAttribute("tabindex")) target.setAttribute("tabindex", "-1")
            target.asDynamic().focus(jsObject<dynamic> { preventScroll = true })
            // replaceState: the address bar follows without adding history entries Kobweb's router would act on
            if (target.id.isNotEmpty()) window.history.replaceState(null, "", "#${target.id}")
        }
        val l = lenis
        if (l == null) {
            target.scrollIntoView() // honours scroll-padding-top; smooth or instant per the CSS motion rules
            arrive()
            return
        }
        // Longer trips take a little longer, but never drag: 0.8s to 1.6s
        val distance = abs(target.getBoundingClientRect().top)
        l.scrollTo(target, jsObject {
            duration = min(1.6, 0.8 + distance / 5000)
            easing = { t -> if (t < 0.5) 4 * t * t * t else 1 - (-2 * t + 2).pow(3) / 2 } // heavy in, heavy out
            onComplete = { arrive() }
        })
    }

    private fun loop(time: Double) {
        lenis?.raf(time)
        emitIfMoved()
        runFrameRequests(time)
        window.requestAnimationFrame(::loop)
    }

    private fun requestTick() {
        if (lenis != null || tickQueued) return
        tickQueued = true
        window.requestAnimationFrame { time ->
            tickQueued = false
            emitIfMoved()
            runFrameRequests(time)
        }
    }

    private fun runFrameRequests(time: Double) {
        if (frameRequests.isEmpty()) return
        // Swapped out first: callbacks that request the next frame land in the fresh list
        val due = frameRequests
        frameRequests = mutableListOf()
        due.forEach { it(time) }
    }

    private fun emitIfMoved() {
        val y = lenis?.scroll ?: window.scrollY
        if (y == lastY && !dirty) return
        lastY = y
        dirty = false
        val frame = ScrollFrame(y, (y / maxScroll).coerceIn(0.0, 1.0), viewportW, viewportH)
        frameCallbacks.forEach { it(frame) }
    }

    private fun measure() {
        viewportW = window.innerWidth.toDouble()
        viewportH = window.innerHeight.toDouble()
        maxScroll = max(1.0, document.documentElement!!.scrollHeight - viewportH)
        measureCallbacks.forEach { it() }
        lenis?.resize()
        dirty = true
        requestTick()
    }

    /** The first-visit intro (boot.js, html[data-boot]) owns the screen until it clears. */
    private fun holdDuringBootIntro() {
        val root = document.documentElement!!
        if (!root.hasAttribute("data-boot")) return
        lock()
        val mo = MutationObserver { _, observer ->
            if (!root.hasAttribute("data-boot")) {
                observer.disconnect()
                unlock()
            }
        }
        mo.observe(root, MutationObserverInit(attributes = true, attributeFilter = arrayOf("data-boot")))
    }

    /** Same-page hash links glide instead of jumping. `data-deploy` links (nav, tracker) flash a deploy line first. */
    private fun onClick(e: Event) {
        val me = e as? MouseEvent ?: return
        if (e.defaultPrevented || me.button.toInt() != 0 || me.metaKey || me.ctrlKey || me.shiftKey || me.altKey) return
        val link = (e.target as? Element)?.closest("a[href]") as? HTMLAnchorElement ?: return
        // The skip link stays an instant native jump
        if (link.classList.contains("skip-link")) return
        val url = URL(link.href)
        if (url.origin != window.location.origin || samePath(url.pathname) != samePath(window.location.pathname)) return
        if (url.hash.length < 2) return
        val target = document.getElementById(js("decodeURIComponent")(url.hash.drop(1)) as String) as? HTMLElement ?: return
        e.preventDefault()
        if (link.hasAttribute("data-deploy")) DeployFlash.play(target.id) { scrollToTarget(target) } else scrollToTarget(target)
    }

    private fun samePath(path: String) = path.removeSuffix("index.html").removeSuffix(".html")
}

/** Marks an inner scroll area so wheel input scrolls it instead of the page. */
fun AttrsScope<*>.scrollArea() {
    attr("data-lenis-prevent", "")
}

/** Plain JS object literal, typed. */
internal inline fun <T> jsObject(init: T.() -> Unit): T = (js("{}") as T).apply(init)
