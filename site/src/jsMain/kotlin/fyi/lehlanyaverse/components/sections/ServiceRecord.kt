package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Chips
import fyi.lehlanyaverse.components.hud.DeclassStamp
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.declassLine
import fyi.lehlanyaverse.components.hud.declassify
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.components.hud.Time
import fyi.lehlanyaverse.data.Intel
import fyi.lehlanyaverse.data.Mission
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Ol
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

/** 03 // SERVICE RECORD: experience, one mission debrief per role, newest first. */
@Composable
fun ServiceRecord() {
    Section({ id("service-record"); attr("aria-labelledby", "service-title") }) {
        SectionHead("03", "Service record", "Experience // mission debriefs", "service-title")
        Ol({ classes("timeline"); attr("role", "list") }) {
            Intel.serviceRecord.forEachIndexed { i, m ->
                Li({ classes("timeline__item") }) { MissionCard(m, i) }
            }
        }
    }
}

@Composable
private fun MissionCard(m: Mission, index: Int) {
    val ongoing = m.end == null
    Panel(
        tag = "article",
        accent = if (ongoing) Accent.ACID else Accent.ORANGE,
        // Text-heavy cards stay straight: rotated body copy rasterises soft
        tilt = Tilt.NONE,
        extraClasses = listOf("mission"),
        attrs = { declassify() },
    ) {
        PanelBar("DEBRIEF" to "M-${(index + 1).toString().padStart(3, '0')}", "UNIT" to m.unit.uppercase())
        Div({ classes("mission__head") }) {
            Div {
                P({ classes("mission__codename"); declassLine(0) }) { Text(m.codename) }
                H3({ classes("mission__role"); declassLine(1) }) {
                    Text(m.role)
                    Span({ classes("mission__unit") }) { Text(" @ ${m.unit}") }
                }
                P({ classes("readout", "mission__dates"); declassLine(2) }) {
                    Time(m.startIso) { Text(m.start) }
                    Text(" → ")
                    if (ongoing) Text("PRESENT") else Time(m.endIso!!) { Text(m.end!!) }
                }
            }
            if (ongoing) Stamp("Active", StampInk.ACID, small = true) else Stamp("Complete", StampInk.ORANGE, small = true)
        }
        P({ classes("mission__brief") }) { Text(m.briefing) }
        P({ classes("readout", "mission__obj-head") }) {
            Text("Objectives completed ")
            Span({ classes("hl-acid") }) { Text("${m.objectives.size}/${m.objectives.size}") }
        }
        Ul({ classes("objectives"); attr("role", "list") }) {
            m.objectives.forEach { o ->
                Li({ classes("objectives__item") }) {
                    Deco("objectives__check") { Text("✓") }
                    Span { Text(o) }
                }
            }
        }
        Chips(m.stack, "Stack used on ${m.role} at ${m.unit}")
        DeclassStamp()
    }
}
