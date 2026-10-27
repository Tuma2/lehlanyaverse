package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Meter
import fyi.lehlanyaverse.components.hud.MeterTone
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.data.Intel
import fyi.lehlanyaverse.data.Skill
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Text

/** 02 // LOADOUT: skills as a game loadout screen. */
@Composable
fun Loadout() {
    val l = Intel.loadout
    Section({ id("loadout"); attr("aria-labelledby", "loadout-title") }) {
        SectionHead("02", "Loadout", "Skills // equipped for deployment", "loadout-title")
        Div({ classes("loadout") }) {
            Slot(
                slot = "1", title = "Primary", sub = "Core weapon systems", skills = l.primary,
                accent = Accent.SIGNAL, tone = MeterTone.SIGNAL, extra = "loadout__primary",
            )
            Slot(
                slot = "2", title = "Secondary", sub = "Currently in training", skills = l.secondary,
                accent = Accent.STEEL, tone = MeterTone.STEEL, tilt = Tilt.RIGHT,
            )
            Slot(
                slot = "3", title = "Equipment", sub = "Tools & platforms", skills = l.equipment,
                accent = Accent.MUTED, tone = MeterTone.MUTED, tilt = Tilt.LEFT,
            )
            Slot(
                slot = "4", title = "Sidearms", sub = "Familiar, field-tested", skills = l.sidearms,
                accent = Accent.INK, tone = MeterTone.INK, extra = "loadout__sidearms",
            )
        }
    }
}

@Composable
private fun Slot(
    slot: String,
    title: String,
    sub: String,
    skills: List<Skill>,
    accent: Accent,
    tone: MeterTone,
    tilt: Tilt = Tilt.NONE,
    extra: String? = null,
) {
    val headingId = "slot-${title.lowercase()}"
    Panel(
        tag = "section",
        accent = accent,
        tilt = tilt,
        extraClasses = listOfNotNull("loadout__slot", extra),
        attrs = { attr("aria-labelledby", headingId) },
    ) {
        PanelBar("SLOT" to slot, "ITEMS" to skills.size.toString().padStart(2, '0'))
        Div({ classes("loadout__head") }) {
            Deco("loadout__num") { Text(slot) }
            Div {
                H3({ id(headingId); classes("loadout__title") }) { Text(title) }
                P({ classes("readout") }) { Text(sub) }
            }
        }
        Div({ classes("loadout__meters") }) {
            skills.forEach { Meter(it.name, it.proficiency, tone) }
        }
    }
}
