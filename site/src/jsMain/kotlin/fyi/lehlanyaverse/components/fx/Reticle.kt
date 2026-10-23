package fyi.lehlanyaverse.components.fx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/** What the reticle locks onto. Keep in step with the lock-cursor rule in hud.css ("CURSOR"). */
private const val INTERACTIVE =
    "a[href], button, summary, label[for], [role='button'], .redact, .pstack__btn, .tracker__link, [data-lock]"

/** Body copy shows the native I-beam instead (hud.css), so the brackets step aside there. */
private const val BODY_COPY =
    "p, dd, blockquote, input, textarea, [contenteditable], li:not(:has(> :is(article, section, aside, div, a)))"

private const val IDLE_HALF = 13.0   // idle brackets: a 26px box around the crosshair
private const val CORNER = 8.0       // bracket arm length (matches .reticle__corner in hud.css)
private const val INFLATE = 6.0      // lock box sits this far outside the target
private const val LOCK_MS = 150.0
private const val PULSE_MS = 180.0
private const val LABEL_MAX = 16

/**
 * TARGET LOCK: four corner brackets around the crosshair cursor that snap out to frame whatever interactive
 * element is under the pointer, turn signal orange and show a short "LOCK // NAME" tag.
 *
 * The crosshair itself is the real CSS cursor (hud.css), so the centre never lags; this layer only adds brackets.
 * Mouse and trackpad only, decorative, never in the export, and it never reacts to (or replaces) keyboard focus.
 */
@Composable
fun Reticle() {
    if (!Fx.CURSOR_FX) return
    var live by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        live = !AppGlobals.isExporting && window.matchMedia("(hover: hover) and (pointer: fine)").matches
    }
    if (!live) return

    var root by remember { mutableStateOf<HTMLElement?>(null) }
    Div({ classes("reticle"); attr("aria-hidden", "true"); ref { root = it; onDispose { root = null } } }) {
        listOf("tl", "tr", "bl", "br").forEach { Span({ classes("reticle__corner", "reticle__corner--$it") }) }
        // Text is written directly by the controller; Compose never renders children here
        Span({ classes("reticle__label") })
    }
    DisposableEffect(root) {
        val el = root ?: return@DisposableEffect onDispose { }
        val controller = ReticleController(el)
        onDispose { controller.dispose() }
    }
}

private class ReticleController(private val root: HTMLElement) {
    private val corners = root.querySelectorAll(".reticle__corner").asList().map { it as HTMLElement }
    private val label = root.querySelector(".reticle__label") as HTMLElement
    private val motion = Fx.motion

    private var x = 0.0
    private var y = 0.0
    private var seen = false
    private var target: Element? = null
    private var frameQueued = false

    // Lock box (viewport coordinates); kept after release so the brackets can ease back from it
    private var lx1 = 0.0
    private var ly1 = 0.0
    private var lx2 = 0.0
    private var ly2 = 0.0

    // Lock progress p: eases from [fromP] towards [goalP] over LOCK_MS, starting at [animStart]
    private var fromP = 0.0
    private var goalP = 0.0
    private var animStart = 0.0
    private var pulseStart = Double.NEGATIVE_INFINITY

    private val onMove: (Event) -> Unit = { e ->
        val d = e.asDynamic()
        if (d.pointerType != "touch") {
            x = (d.clientX as Number).toDouble()
            y = (d.clientY as Number).toDouble()
            if (!seen) {
                seen = true
                root.classList.add("is-on")
            }
            root.classList.remove("is-out")
            retarget(e.target as? Element)
            schedule()
        }
    }

    private val onDown: (Event) -> Unit = { e ->
        if (motion && e.asDynamic().pointerType != "touch") {
            pulseStart = window.performance.now()
            schedule()
        }
    }

    // Leaving the window: relatedTarget is null when the pointer goes outside the document
    private val onOut: (Event) -> Unit = { e ->
        if (e.asDynamic().relatedTarget == null) root.classList.add("is-out")
    }

    private val passive: dynamic = jsObject<dynamic> { this.passive = true }

    // Content gliding under a still pointer (smooth scroll, a deploy jump) changes what it's over
    private val unScroll = SmoothScroll.onFrame {
        if (seen) {
            retarget(document.elementFromPoint(x, y))
            schedule()
        }
    }

    init {
        window.addEventListener("pointermove", onMove, passive)
        window.addEventListener("pointerdown", onDown, passive)
        document.addEventListener("pointerout", onOut, passive)
    }

    fun dispose() {
        window.removeEventListener("pointermove", onMove, passive)
        window.removeEventListener("pointerdown", onDown, passive)
        document.removeEventListener("pointerout", onOut, passive)
        unScroll()
    }

    private fun retarget(el: Element?) {
        val next = el?.closest(INTERACTIVE)
        root.classList.toggle("is-text", next == null && el?.closest(BODY_COPY) != null)
        if (next == target) return
        target = next
        if (next != null) {
            label.textContent = lockLabel(next)
            root.classList.add("is-locked")
        } else {
            root.classList.remove("is-locked")
        }
        animateTo(if (next != null) 1.0 else 0.0)
    }

    private fun animateTo(goal: Double) {
        val now = window.performance.now()
        fromP = progress(now)
        goalP = goal
        animStart = now
    }

    private fun progress(now: Double): Double {
        if (!motion) return goalP
        val t = ((now - animStart) / LOCK_MS).coerceIn(0.0, 1.0)
        return fromP + (goalP - fromP) * (1 - (1 - t).pow(3)) // ease-out cubic
    }

    private fun schedule() {
        if (frameQueued) return
        frameQueued = true
        SmoothScroll.requestFrame(::frame)
    }

    private fun frame(time: Double) {
        frameQueued = false
        val now = window.performance.now()

        // Reads first: the locked element may have moved (scroll) or gone (re-render, route change)
        val t = target
        if (t != null && !t.isConnected) {
            target = null
            root.classList.remove("is-locked")
            animateTo(0.0)
        } else if (t != null) {
            val r = t.getBoundingClientRect()
            lx1 = r.left - INFLATE
            ly1 = r.top - INFLATE
            lx2 = r.right + INFLATE
            ly2 = r.bottom + INFLATE
        }

        // Idle box tracks the pointer exactly; only the lock expansion is eased
        val p = progress(now)
        var x1 = lerp(x - IDLE_HALF, lx1, p)
        var y1 = lerp(y - IDLE_HALF, ly1, p)
        var x2 = lerp(x + IDLE_HALF, lx2, p)
        var y2 = lerp(y + IDLE_HALF, ly2, p)

        // Shot fired: the box pulls in briefly around its own centre
        val sincePulse = now - pulseStart
        val pulsing = motion && sincePulse < PULSE_MS
        if (pulsing) {
            val k = 1 - 0.18 * sin(PI * sincePulse / PULSE_MS)
            val cx = (x1 + x2) / 2
            val cy = (y1 + y2) / 2
            x1 = cx - (cx - x1) * k
            x2 = cx + (x2 - cx) * k
            y1 = cy - (cy - y1) * k
            y2 = cy + (y2 - cy) * k
        }

        // Writes: transforms only
        place(corners[0], x1, y1)
        place(corners[1], x2 - CORNER, y1)
        place(corners[2], x1, y2 - CORNER)
        place(corners[3], x2 - CORNER, y2 - CORNER)
        // Tag above the box, or below it when the box touches the top of the window
        place(label, x1, if (y1 > 24) y1 - 22 else y2 + 6)

        if (pulsing || p != goalP) schedule()
    }

    private fun place(el: HTMLElement, px: Double, py: Double) {
        el.style.transform = "translate3d(${px}px, ${py}px, 0)"
    }
}

private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

/**
 * "LOCK // GITHUB": an explicit data-lock name, else the element's visible text (skipping aria-hidden parts like
 * section numbers and arrows), else its aria-label. One line, truncated.
 */
private fun lockLabel(el: Element): String {
    val raw = el.getAttribute("data-lock")?.takeIf { it.isNotBlank() }
        ?: visibleText(el).takeIf { it.isNotBlank() }
        ?: el.getAttribute("aria-label")
        ?: ""
    val clean = raw.replace(Regex("\\s+"), " ").trim().uppercase()
    if (clean.isEmpty()) return "LOCK"
    val short = if (clean.length > LABEL_MAX) clean.take(LABEL_MAX - 1).trimEnd() + "…" else clean
    return "LOCK // $short"
}

private fun visibleText(node: Node): String = buildString {
    node.childNodes.asList().forEach { child ->
        when {
            child.nodeType == Node.TEXT_NODE -> append(child.textContent)
            child is Element && child.getAttribute("aria-hidden") != "true" -> append(visibleText(child))
        }
    }
}
