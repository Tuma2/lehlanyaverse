package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.Redacted
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Tape
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.components.hud.TypeLine
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/** 01 // OPERATOR PROFILE: the hero. Holds the page's only <h1>. */
@Composable
fun OperatorProfile() {
    val op = Intel.operator
    val (first, last) = op.name.substringBefore(' ') to op.name.substringAfter(' ')

    Section({ id("profile"); classes("hero"); attr("aria-labelledby", "profile-title") }) {
        Div({ classes("hero__grid") }) {
            Div {
                P({ classes("hero__kicker") }) { TypeLine("> 01 // INCOMING DOSSIER: OPERATOR PROFILE") }
                H1({ id("profile-title"); classes("hero__name") }) {
                    Span { Text(first) }
                    Span({ classes("outline") }) { Text(last) }
                }
                P({ classes("hero__aka") }) {
                    Text("AKA ")
                    Redacted(op.callsign)
                    Deco("redact-hint") { Text("  [ HOVER / TAP TO DECLASSIFY ]") }
                }
                Div({ classes("hero__role") }) {
                    Span({ classes("hero__role-chip") }) { Text(op.role) }
                    Span({ classes("hero__role-chip", "hero__role-chip--ghost") }) { Text(op.specialty) }
                }

                Panel(accent = Accent.BONE, extraClasses = listOf("hero__bio-panel"), attrs = { style { property("margin-top", "2rem") } }) {
                    PanelBar("BRIEF" to "OPERATOR SUMMARY", "SRC" to "HQ // JHB")
                    Div({ classes("hero__bio") }) {
                        op.bio.forEach { P { Text(it) } }
                    }
                    Div({ classes("hero__actions") }) {
                        A("#loadout", { classes("btn", "btn--solid") }) {
                            Text("Inspect loadout")
                            Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("↓") }
                        }
                        A("#comms", { classes("btn", "btn--acid") }) {
                            Text("Open comms")
                            Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("→") }
                        }
                    }
                }
            }

            Div({ classes("hero__side") }) {
                IdCard()
                StatsBoard()
            }
        }
    }
}

@Composable
private fun IdCard() {
    val op = Intel.operator
    val initials = op.name.split(' ').joinToString("") { it.take(1) }
    Panel(tag = "aside", accent = Accent.ACID, tilt = Tilt.RIGHT, attrs = { attr("aria-label", "Operator ID card") }) {
        Tape()
        PanelBar("ID" to "OP-0001", "CLR" to op.clearance)
        Div({ classes("idcard") }) {
            // TODO(sebabatso): swap the initials for a photo: an <img> with real alt text
            Div({ classes("idcard__photo"); attr("aria-hidden", "true") }) { Text(initials) }
            Div({ classes("idcard__rows") }) {
                IdRow("Status") { Span({ classes("status-live") }) { Text(op.status) } }
                IdRow("Role") { Text("${op.role} // ${op.specialty}") }
                IdRow("AO") { Text(op.location) }
                IdRow("Grid") { Text(op.coordinates) }
            }
        }
        Div({ classes("hero__stamps") }) {
            Stamp("Classified", StampInk.RED, small = true, struck = true)
            Stamp("Declassified", StampInk.ACID, small = true)
        }
    }
}

@Composable
private fun IdRow(label: String, value: @Composable () -> Unit) {
    Div({ classes("idcard__row") }) {
        Span({ classes("readout") }) { Text(label) }
        Span({ classes("idcard__val") }) { value() }
    }
}

@Composable
private fun StatsBoard() {
    Panel(tag = "aside", accent = Accent.TAPE, tilt = Tilt.LEFT, extraClasses = listOf("panel--olive", "stats"), attrs = { attr("aria-label", "Field statistics") }) {
        PanelBar("AAR" to "FIELD STATS", "REV" to "2026")
        Div({ classes("stats__grid") }) {
            Intel.operator.stats.forEach { s ->
                Div({ classes("stats__cell") }) {
                    Span({ classes("stats__value") }) { Text(s.value) }
                    Span({ classes("stats__label", "readout") }) { Text(s.label) }
                }
            }
        }
        Div({ classes("sticker", "stats__sticker"); attr("aria-hidden", "true") }) {
            Span { Text("Now"); Span({ classes("sticker__big") }) { Text("Kotlin") }; Text("training") }
        }
    }
}
