package fyi.lehlanyaverse.components.fx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/** Scroll speed (px/ms) that reads as fully exposed. */
private const val FULL_SPEED = 2.4

/** Per-frame decay once the visitor holds still. */
private const val DECAY = 0.93

/**
 * Light meter for the telemetry strip: "VIS ▮▮▮▯▯ HIDDEN". Moving through the dossier is noise and light, so the
 * meter rises with scroll speed and sinks back into shadow when the visitor stops. Decorative (the strip is
 * aria-hidden). Without motion it stays at rest, reading HIDDEN.
 *
 * The bar is a transform on its own layer and the label only changes on a state change, so a frame never
 * repaints the blurred header around it.
 */
@Composable
fun VisibilityMeter() {
    if (!Fx.VISIBILITY) return
    var fill by remember { mutableStateOf<HTMLElement?>(null) }
    var state by remember { mutableStateOf("HIDDEN") }

    Span({ classes("vis"); attr("data-state", state.lowercase()) }) {
        Span({ classes("vis__label") }) { Text("VIS") }
        Span({ classes("vis__track") }) {
            Span({ classes("vis__fill"); ref { fill = it; onDispose { fill = null } } })
        }
        Span({ classes("vis__state") }) { Text(state) }
    }

    DisposableEffect(Unit) {
        if (!Fx.motion) return@DisposableEffect onDispose { }
        var level = 0.0
        var shown = -1.0
        var lastY = Double.NaN
        var lastT = 0.0
        var decaying = false

        fun paint() {
            val q = (level * 40).roundToInt() / 40.0
            if (q != shown) {
                fill?.style?.transform = "scaleX($q)"
                shown = q
            }
            val s = when {
                level < 0.34 -> "HIDDEN"
                level < 0.67 -> "MASKED"
                else -> "EXPOSED"
            }
            if (s != state) state = s
        }

        fun decay(@Suppress("UNUSED_PARAMETER") t: Double) {
            level *= DECAY
            if (level < 0.01) level = 0.0
            paint()
            decaying = level > 0.0
            if (decaying) SmoothScroll.requestFrame(::decay)
        }

        val dispose = SmoothScroll.onFrame { f ->
            val now = window.performance.now()
            if (!lastY.isNaN() && now > lastT) {
                val speed = abs(f.y - lastY) / (now - lastT)
                val target = min(1.0, speed / FULL_SPEED)
                // Rises fast, falls slowly
                if (target > level) level += (target - level) * 0.35
                paint()
                if (!decaying) {
                    decaying = true
                    SmoothScroll.requestFrame(::decay)
                }
            }
            lastY = f.y
            lastT = now
        }
        onDispose { dispose() }
    }
}
