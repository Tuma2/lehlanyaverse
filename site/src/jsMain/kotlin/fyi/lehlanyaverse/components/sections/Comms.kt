package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Hazard
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.data.CommsChannel
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

/** 07 // COMMS: contact channels as big tap targets. Deliberately no phone number. */
@Composable
fun Comms() {
    Section({ id("comms"); attr("aria-labelledby", "comms-title") }) {
        SectionHead("07", "Comms", "Contact // open a channel", "comms-title")
        Panel(accent = Accent.SIGNAL, extraClasses = listOf("comms")) {
            PanelBar("CHANNELS" to "${Intel.comms.size} OPEN", "BAND" to "EMAIL + SOCIAL")
            P({ classes("comms__lede") }) {
                Text("Hiring, collaborating or just want to talk backend systems? Pick a channel.")
            }
            Ul({ classes("comms__list"); attr("role", "list") }) {
                Intel.comms.forEachIndexed { i, c ->
                    Li {
                        A(c.url, {
                            classes("comms__link")
                            if (c.channel != CommsChannel.EMAIL) attr("rel", "noopener noreferrer")
                        }) {
                            Deco("comms__idx") { Text("CH-${(i + 1).toString().padStart(2, '0')}") }
                            Span({ classes("comms__label") }) { Text(c.label) }
                            Span({ classes("comms__handle") }) { Text(c.handle) }
                            Deco("comms__arrow") { Text("→") }
                        }
                    }
                }
            }
            Div({ classes("comms__stamp") }) { Stamp("Transmission open", StampInk.SIGNAL, small = true) }
        }
        Hazard()
    }
}
