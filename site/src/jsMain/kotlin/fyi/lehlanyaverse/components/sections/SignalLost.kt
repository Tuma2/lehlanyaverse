package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.navigation.Anchor
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/** Shared "nothing here" screen for the 404 page and unknown operation slugs. */
@Composable
fun SignalLost(message: String) {
    Section({ classes("lost"); attr("aria-labelledby", "lost-title") }) {
        Panel(accent = Accent.ORANGE) {
            PanelBar("ERR" to "404", "LINK" to "SEVERED")
            P({ classes("readout", "hl-orange") }) { Text("> CARRIER LOST") }
            H1({ id("lost-title"); classes("lost__title") }) { Text("Signal lost") }
            P({ classes("lost__msg") }) { Text(message) }
            Div({ classes("hero__actions") }) {
                Anchor("/", { classes("btn", "btn--solid") }) {
                    Text("Return to dossier")
                    Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("→") }
                }
            }
            Div({ classes("lost__stamp") }) { Stamp("Redacted", StampInk.RED) }
        }
    }
}
