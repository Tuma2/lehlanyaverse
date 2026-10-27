package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.navigation.Anchor
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.RedactedLines
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.litOnView
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/**
 * Shared "nothing here" screen for the 404 page and unknown operation slugs: the calm panel with the way back,
 * and behind it the page's one burst, a stamped and redacted TRANSMISSION SUSPENDED collage.
 */
@Composable
fun SignalLost(message: String) {
    Section({ classes("lost", "burst"); attr("aria-labelledby", "lost-title"); litOnView() }) {
        Deco("burst__spot")
        Suspended()
        Panel(accent = Accent.STEEL, extraClasses = listOf("lost__panel")) {
            PanelBar("ERR" to "404", "LINK" to "SEVERED")
            P({ classes("readout", "hl-steel") }) { Text("> CARRIER LOST") }
            H1({ id("lost-title"); classes("lost__title") }) { Text("Signal lost") }
            P({ classes("lost__msg") }) { Text(message) }
            Div({ classes("hero__actions") }) {
                Anchor("/", { classes("btn", "btn--solid") }) {
                    Text("Return to dossier")
                    Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("→") }
                }
            }
        }
    }
}

/** The collage: oversized cut-up headline, a redacted transmission log, stamps and a violet halftone. Decorative. */
@Composable
private fun Suspended() {
    Div({ classes("suspended", "burst__dim"); attr("aria-hidden", "true") }) {
        Layer("suspended__halftone")
        Layer("suspended__word", "suspended__word--a", rot = -4) { Text("Transmission") }
        Layer("suspended__word", "suspended__word--b", rot = 3) { Text("Suspended") }
        Layer("suspended__log", rot = 6) {
            Span({ classes("collage__dossier-head") }) { Text("TX LOG // CHANNEL 07") }
            RedactedLines(
                "02:14:07 carrier dropped at #relay",
                "02:14:09 retry via #jhb-06 failed",
                "02:14:12 handler #notified. hold",
                "02:14:15 position. await #signal.",
            )
        }
        Layer("suspended__stamp", "suspended__stamp--a", rot = -16) { Stamp("Suspended", StampInk.CLASSIFIED) }
        Layer("suspended__stamp", "suspended__stamp--b", rot = 11) { Stamp("Redacted", StampInk.CLASSIFIED, small = true) }
        Layer("suspended__stamp", "suspended__stamp--c", rot = -6) { Stamp("Stand by", StampInk.AMBER, small = true) }
    }
}
