package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Tape
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.components.hud.Time
import fyi.lehlanyaverse.data.FieldNote
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

/** 06 // FIELD INTEL: things worth investing time in. Taped index cards; add one by appending to Intel.fieldIntel. */
@Composable
fun FieldIntel() {
    Section({ id("field-intel"); attr("aria-labelledby", "intel-title") }) {
        SectionHead("06", "Field intel", "Interests // currently investing time in", "intel-title")
        Ul({ classes("intel"); attr("role", "list") }) {
            Intel.fieldIntel.forEachIndexed { i, note -> Li { IntelCard(note, i) } }
        }
    }
}

@Composable
private fun IntelCard(note: FieldNote, index: Int) {
    // Cycle through card treatments so a long list stays a collage, not a grid of clones
    val paper = index % 3 == 1
    Panel(
        tag = "article",
        accent = if (index % 3 == 2) Accent.ACID else Accent.TAPE,
        tilt = listOf(Tilt.LEFT, Tilt.RIGHT, Tilt.NONE)[index % 3],
        extraClasses = listOfNotNull("intel__card", "panel--paper".takeIf { paper }),
    ) {
        Tape(if (index % 2 == 0) null else "right")
        P({ classes("readout", "intel__meta") }) {
            Span { Text("INTEL #${(index + 1).toString().padStart(2, '0')}") }
            Span { Text("LOGGED "); Time(note.logged) { Text(note.logged) } }
        }
        H3({ classes("intel__title") }) { Text(note.title) }
        P({ classes("intel__note") }) { Text(note.note) }
        Div({ classes("intel__foot") }) {
            P({ classes("intel__tags") }) {
                note.tags.forEach { Span({ classes("intel__tag") }) { Text("#$it") } }
            }
            if (note.url != null) {
                A(note.url, {
                    classes("intel__src")
                    attr("rel", "noopener noreferrer")
                    attr("aria-label", "Source for: ${note.title}")
                }) { Text("Source →") }
            }
        }
    }
}
