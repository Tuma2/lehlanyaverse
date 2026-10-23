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
import fyi.lehlanyaverse.components.layout.dossierSections
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Ol
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.Element

/** Minimal binding: IntersectionObserver isn't part of Kotlin/JS's bundled DOM API. */
private external class IntersectionObserver(
    callback: (entries: Array<dynamic>, observer: IntersectionObserver) -> Unit,
    options: dynamic = definedExternally,
) {
    fun observe(target: Element)
    fun disconnect()
}

/**
 * SECTOR TRACKER: a waypoint rail on the right edge of wide screens, one marker per dossier section. A bracket
 * reticle sits on the current sector; clicking a waypoint deploys there.
 *
 * It repeats the header nav, which remains the accessible route, so the rail is hidden from screen readers and
 * kept out of the tab order. Rendered only once the app runs (never in the static export).
 */
@Composable
fun SectorTracker() {
    if (!Fx.TRACKER) return
    var live by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { live = !AppGlobals.isExporting }
    if (!live) return

    // The current sector is whichever section crosses a thin band just above the middle of the viewport
    DisposableEffect(Unit) {
        val io = IntersectionObserver({ entries, _ ->
            entries.filter { it.isIntersecting as Boolean }.forEach { e ->
                val i = dossierSections.indexOfFirst { it.first == (e.target as Element).id }
                if (i >= 0) current = i
            }
        }, js("({ rootMargin: '-45% 0px -50% 0px' })"))
        dossierSections.forEach { (id, _) -> document.getElementById(id)?.let(io::observe) }
        onDispose { io.disconnect() }
    }

    Div({
        classes("tracker")
        attr("aria-hidden", "true")
        style { property("--cur", current.toString()) }
    }) {
        Span({ classes("tracker__reticle") })
        Ol({ classes("tracker__list"); attr("role", "list") }) {
            dossierSections.forEachIndexed { i, (id, label) ->
                Li({ classes(*listOfNotNull("tracker__item", "is-current".takeIf { i == current }).toTypedArray()) }) {
                    A("/#$id", { classes("tracker__link"); tabIndex(-1); attr("data-deploy", ""); attr("data-lock", label.uppercase()) }) {
                        // The number shows on the current sector; the name only on hover, so the rail never
                        // sits over the dossier for long
                        Span({ classes("tracker__label") }) {
                            Span({ classes("tracker__name") }) { Text(label.uppercase()) }
                            Span({ classes("tracker__num") }) { Text((i + 1).toString().padStart(2, '0')) }
                        }
                        Span({ classes("tracker__tick") })
                    }
                }
            }
        }
    }
}
