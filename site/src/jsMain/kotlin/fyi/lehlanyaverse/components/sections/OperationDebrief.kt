package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.navigation.Anchor
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Chips
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.OpLinks
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.StatusTag
import fyi.lehlanyaverse.components.hud.Tape
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.components.hud.TypeLine
import fyi.lehlanyaverse.data.Intel
import fyi.lehlanyaverse.data.OpStatus
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLElement

/** Full debrief for one operation. Everything comes from Intel.operations[index]. */
@Composable
fun OperationDebrief(index: Int) {
    val op = Intel.operations[index]
    val ops = Intel.operations

    Section({ classes("debrief"); attr("aria-labelledby", "debrief-title") }) {
        Anchor("/#operations", { classes("debrief__back", "readout") }) { Text("← All operations") }

        Div({ classes("debrief__hero") }) {
            P({ classes("hero__kicker") }) { TypeLine("> 05 // OPERATION DEBRIEF: ${op.codename}") }
            H1({ id("debrief-title"); classes("debrief__title") }) { Text(op.title) }
            Div({ classes("debrief__status") }) {
                StatusTag(op.status)
                Span({ classes("readout") }) { Text("FILE ${op.slug.uppercase()}") }
            }
            P({ classes("debrief__summary") }) { Text(op.summary) }
            Div({ classes("hero__actions") }) { OpLinks(op) }
            Div({ classes("debrief__stamp") }) {
                when (op.status) {
                    OpStatus.DEPLOYED -> Stamp("Deployed", StampInk.ACID)
                    OpStatus.IN_PROGRESS -> Stamp("In progress", StampInk.ORANGE)
                    OpStatus.RECON -> Stamp("Recon", StampInk.RED)
                }
            }
        }

        Div({ classes("debrief__grid") }) {
            Panel(tag = "section", accent = Accent.BONE, extraClasses = listOf("debrief__brief"), attrs = { attr("aria-labelledby", "brief-h") }) {
                PanelBar("SECTION" to "A", "TYPE" to "BRIEFING")
                H2({ id("brief-h"); classes("debrief__h") }) { Text("Briefing") }
                op.briefing.forEach { P { Text(it) } }
            }

            Panel(tag = "section", accent = Accent.ACID, tilt = Tilt.RIGHT, attrs = { attr("aria-labelledby", "obj-h") }) {
                Tape()
                PanelBar("SECTION" to "B", "COUNT" to op.objectives.size.toString().padStart(2, '0'))
                H2({ id("obj-h"); classes("debrief__h") }) { Text("Objectives") }
                Ul({ classes("objectives"); attr("role", "list") }) {
                    op.objectives.forEach { o ->
                        Li({ classes("objectives__item") }) {
                            Deco("objectives__check") { Text(if (op.status == OpStatus.DEPLOYED) "✓" else "▢") }
                            Span { Text(o) }
                        }
                    }
                }
            }

            Panel(tag = "section", accent = Accent.TAPE, tilt = Tilt.LEFT, extraClasses = listOf("panel--olive"), attrs = { attr("aria-labelledby", "stack-h") }) {
                PanelBar("SECTION" to "C", "TYPE" to "LOADOUT")
                H2({ id("stack-h"); classes("debrief__h") }) { Text("Stack") }
                Chips(op.stack, "${op.title} stack")
            }

            Panel(tag = "section", accent = Accent.ORANGE, extraClasses = listOf("debrief__visual"), attrs = { attr("aria-labelledby", "visual-h") }) {
                PanelBar("SECTION" to "D", "TYPE" to "VISUAL INTEL")
                H2({ id("visual-h"); classes("debrief__h") }) { Text("Visual intel") }
                val shot = op.screenshot
                if (shot != null) {
                    TagElement<HTMLElement>("figure", { classes("debrief__figure") }) {
                        Img(shot.src, shot.alt, { attr("loading", "lazy"); attr("decoding", "async") })
                    }
                } else {
                    Div({ classes("debrief__placeholder") }) {
                        Span({ classes("readout") }) { Text("Imagery pending declassification") }
                    }
                }
            }
        }

        if (ops.size > 1) {
            Nav({ classes("debrief__pager"); attr("aria-label", "Other operations") }) {
                val prev = ops[(index - 1 + ops.size) % ops.size]
                val next = ops[(index + 1) % ops.size]
                // With exactly two ops, previous and next are the same page: show it once
                if (prev != next) {
                    Anchor("/operations/${prev.slug}", { classes("pager__link") }) {
                        Span({ classes("readout") }) { Text("← Previous op") }
                        Span({ classes("pager__title") }) { Text(prev.title) }
                    }
                }
                Anchor("/operations/${next.slug}", { classes("pager__link", "pager__link--next") }) {
                    Span({ classes("readout") }) { Text("Next op →") }
                    Span({ classes("pager__title") }) { Text(next.title) }
                }
            }
        }
    }
}
