package fyi.lehlanyaverse.components.hud

import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import fyi.lehlanyaverse.components.fx.Fx
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.Element

/** Minimal binding: IntersectionObserver isn't part of Kotlin/JS's bundled DOM API. */
private external class IntersectionObserver(
    callback: (entries: Array<dynamic>, observer: IntersectionObserver) -> Unit,
    options: dynamic = definedExternally,
) {
    fun observe(target: Element)
    fun unobserve(target: Element)
    fun disconnect()
}

/**
 * Adds `is-seen` the first time the element scrolls into view, which CSS uses to play a one-shot entrance.
 * Skipped during static export so the snapshot always holds the final, fully rendered state.
 */
fun AttrsScope<*>.markWhenSeen() {
    ref { el ->
        if (AppGlobals.isExporting) return@ref onDispose { }
        val io = IntersectionObserver({ entries, obs ->
            if (entries.any { it.isIntersecting as Boolean }) {
                el.classList.add("is-seen")
                obs.disconnect()
            }
        }, js("({ threshold: 0.35 })"))
        io.observe(el)
        onDispose { io.disconnect() }
    }
}

/**
 * One-shot entrance for content that must never be hidden from someone already reading it (declassify bars,
 * typed sector headers). The element is pre-rendered in its finished state. Once the app is running, only
 * elements still *below the fold* are armed (`fx-armed`, which CSS uses to hide or redact them), and each one
 * plays (`is-seen`) as it scrolls into view. Anything already on screen or scrolled past is left alone.
 *
 * Arming waits for one frame and is batched, so all positions are read before any class is written
 * (no layout thrash while the page mounts). Off entirely without motion.
 */
fun AttrsScope<*>.revealOnEntry() {
    ref { el ->
        if (!Fx.motion) return@ref onDispose { }
        Reveal.queue(el)
        onDispose { Reveal.forget(el) }
    }
}

/** Anything that comes into view this soon after arming was really on screen at mount: shown finished, no replay. */
private const val SETTLE_MS = 600.0

private object Reveal {
    private val pending = mutableListOf<Element>()
    private var scheduled = false
    private var armedAt = 0.0

    // Plays as the element's top edge crosses 85% of the viewport height, whatever its size
    private val io by lazy {
        IntersectionObserver({ entries, obs ->
            entries.filter { it.isIntersecting as Boolean }.forEach {
                val el = it.target as Element
                if (window.performance.now() - armedAt < SETTLE_MS) el.classList.remove("fx-armed") else el.classList.add("is-seen")
                obs.unobserve(el)
            }
        }, js("({ rootMargin: '0px 0px -15% 0px', threshold: 0 })"))
    }

    fun queue(el: Element) {
        pending += el
        if (scheduled) return
        scheduled = true
        // Two frames: Kobweb restores a #fragment's scroll position just after mounting, and the check has to
        // see where the visitor actually is
        window.requestAnimationFrame {
            window.requestAnimationFrame {
                scheduled = false
                val fold = window.innerHeight
                val below = pending.filter { it.isConnected && it.getBoundingClientRect().top > fold }
                pending.clear()
                armedAt = window.performance.now()
                below.forEach {
                    it.classList.add("fx-armed")
                    io.observe(it)
                }
            }
        }
    }

    fun forget(el: Element) {
        pending -= el
        io.unobserve(el)
    }
}
