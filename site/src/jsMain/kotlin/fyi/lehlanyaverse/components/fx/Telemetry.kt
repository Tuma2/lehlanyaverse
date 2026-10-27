package fyi.lehlanyaverse.components.fx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.Text as TextNode
import kotlin.math.roundToInt
import kotlin.math.sin

/** Johannesburg CBD: where the camera starts. */
private const val START_LAT = 26.2041
private const val START_LON = 28.0473

/** Readout refresh interval while scrolling. */
private const val TEXT_INTERVAL_MS = 66.0

/**
 * The header's telemetry strip: MISSION PROGRESS bar + readout, the light meter ([VisibilityMeter]), and fake
 * coordinates that pan with scroll as if the camera were moving across a map. Decorative for screen readers (it changes every frame).
 *
 * The pre-rendered HTML holds the start values; frames then rewrite the existing text nodes in place, and only
 * when the shown value changes. The readouts have fixed widths, so an update never moves anything else.
 */
@Composable
fun Telemetry() {
    if (!Fx.PROGRESS && !Fx.COORDINATES && !Fx.VISIBILITY) return
    val refs = remember { mutableMapOf<String, HTMLElement>() }

    Div({ classes("telemetry"); attr("aria-hidden", "true") }) {
        if (Fx.PROGRESS) {
            Div({ classes("telemetry__track") }) {
                Div({ classes("telemetry__fill"); ref { refs["fill"] = it; onDispose { } } })
            }
        }
        Div({ classes("wrap", "telemetry__inner") }) {
            if (Fx.PROGRESS) {
                Span({ classes("telemetry__progress") }) {
                    Span({ classes("telemetry__label") }) { Text("MISSION ") }
                    Text("PROGRESS ")
                    Span({ classes("telemetry__pct"); ref { refs["pct"] = it; onDispose { } } }) { Text("000%") }
                }
            }
            VisibilityMeter()
            if (Fx.COORDINATES) {
                Span({ classes("telemetry__coords") }) {
                    Span({ classes("telemetry__ll"); ref { refs["ll"] = it; onDispose { } } }) { Text(latLon(START_LAT, START_LON)) }
                    Span({ classes("telemetry__grid"); ref { refs["grid"] = it; onDispose { } } }) { Text(gridRef(START_LAT, START_LON)) }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        if (AppGlobals.isExporting) return@DisposableEffect onDispose { }
        val fill = refs["fill"]
        val pct = refs["pct"]?.textNode()
        val ll = refs["ll"]?.textNode()
        val grid = refs["grid"]?.textNode()
        var lastScale = -1.0
        var lastText = 0.0
        var trailing = 0
        fun writeText(f: ScrollFrame) {
            lastText = window.performance.now()
            pct?.update("${(f.progress * 100).roundToInt().toString().padStart(3, '0')}%")
            if (ll != null || grid != null) {
                // Heads south-east across the city over the whole page, with a slow drift either side
                val lat = START_LAT + 0.09 * f.progress + 0.0012 * sin(f.y / 700)
                val lon = START_LON + 0.14 * f.progress + 0.0015 * sin(f.y / 900)
                ll?.update(latLon(lat, lon))
                grid?.update(gridRef(lat, lon))
            }
        }
        val dispose = SmoothScroll.onFrame { f ->
            if (fill != null) {
                // Quantised to 1/1000 so an idle wobble in the scroll value never re-composites the bar
                val scale = (f.progress * 1000).roundToInt() / 1000.0
                if (scale != lastScale) {
                    fill.style.transform = "scaleX($scale)"
                    lastScale = scale
                }
            }
            // Text repaints the (blurred) header, so the readouts tick at ~15Hz while the bar runs every frame.
            // A trailing write makes sure they always settle on the exact final value.
            window.clearTimeout(trailing)
            if (window.performance.now() - lastText >= TEXT_INTERVAL_MS) writeText(f)
            else trailing = window.setTimeout({ writeText(f) }, TEXT_INTERVAL_MS.toInt())
        }
        onDispose { dispose(); window.clearTimeout(trailing) }
    }
}

private fun HTMLElement.textNode(): TextNode? = firstChild as? TextNode

private fun TextNode.update(value: String) {
    if (data != value) data = value
}

private fun fixed4(v: Double): String = v.asDynamic().toFixed(4) as String

private fun latLon(lat: Double, lon: Double) = "${fixed4(lat)}°S ${fixed4(lon)}°E"

/** In-world grid reference: the zone is Johannesburg's real one, the digits just follow the position. */
private fun gridRef(lat: Double, lon: Double): String {
    fun four(v: Double) = ((v * 100_000).toLong() % 10_000).toString().padStart(4, '0')
    return "GRID 35J NQ ${four(lon)} ${four(lat)}"
}
