package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.fx.parallax
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Beacon
import fyi.lehlanyaverse.components.hud.CamStamp
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.PolaroidStack
import fyi.lehlanyaverse.components.hud.Redacted
import fyi.lehlanyaverse.components.hud.RedactedLines
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Tape
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.components.hud.TypeLine
import fyi.lehlanyaverse.components.hud.litOnView
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/**
 * 01 // OPERATOR PROFILE: the hero, and the first burst moment. A layered case-file collage around the ID card,
 * an oversized name cut through by a CLASSIFIED band and an intel ticker. Everything that matters (name, role,
 * beacon, bio) sits above the collage on solid backing, so it reads first. Holds the page's only <h1>.
 */
@Composable
fun OperatorProfile() {
    val op = Intel.operator
    val (first, last) = op.name.substringBefore(' ') to op.name.substringAfter(' ')

    Section({ id("profile"); classes("hero", "burst"); attr("aria-labelledby", "profile-title"); litOnView() }) {
        Deco("burst__spot")
        Div({ classes("hero__grid") }) {
            Div({ classes("hero__main") }) {
                P({ classes("hero__kicker") }) { TypeLine("> 01 // INCOMING DOSSIER: OPERATOR PROFILE") }
                Div({ classes("hero__plate") }) {
                    // The band runs behind the letters: it shows through the outlined surname, never over the fill
                    Layer("hero__classified", "burst__dim", rot = -5) {
                        repeat(2) { Span { Text("Classified") } }
                    }
                    H1({ id("profile-title"); classes("hero__name") }) {
                        Span { Text(first) }
                        Span({ classes("outline") }) { Text(last) }
                    }
                }
                HeroTicker()
                P({ classes("hero__aka") }) {
                    Text("AKA ")
                    Redacted(op.callsign)
                    Deco("redact-hint") { Text("[ HOVER / TAP TO DECLASSIFY ]") }
                }
                Div({ classes("hero__role") }) {
                    Span({ classes("hero__role-chip") }) { Text(op.role) }
                    Span({ classes("hero__role-chip", "hero__role-chip--ghost") }) { Text(op.specialty) }
                }

                Panel(accent = Accent.INK, extraClasses = listOf("hero__bio-panel"), attrs = { style { property("margin-top", "2rem") } }) {
                    PanelBar("BRIEF" to "OPERATOR SUMMARY", "SRC" to "HQ // JHB")
                    Div({ classes("hero__bio") }) {
                        op.bio.forEach { P { Text(it) } }
                    }
                    Div({ classes("hero__actions") }) {
                        A("#loadout", { classes("btn", "btn--solid") }) {
                            Text("Inspect loadout")
                            Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("↓") }
                        }
                        A("#comms", { classes("btn", "btn--signal") }) {
                            Text("Open comms")
                            Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("→") }
                        }
                    }
                }
            }

            Div({ classes("hero__side") }) {
                // Sonar sweep behind the ID card: concentric rings + pulses (CSS). Purely decorative
                Deco("sonar") { repeat(3) { Span({ classes("sonar__pulse") }) } }
                Collage()
                IdCard()
                StatsBoard()
            }
        }
    }
}

/**
 * Intel lines scrolling under the name. Decorative (every line repeats something stated elsewhere), so hidden
 * from screen readers; it pauses on hover and with the header ticker's pause button (hud.css 8e, 9).
 */
@Composable
private fun HeroTicker() {
    val op = Intel.operator
    val lines = listOfNotNull(
        "Subject ${op.name}",
        "Clearance ${op.clearance}",
        "Last seen ${op.location}",
        "Grid ${op.coordinates}",
        "Primary ${Intel.loadout.primary.take(2).joinToString(" + ") { it.name }}",
        "Open to new assignment".takeIf { op.openToOpportunities },
        "Status ${op.status}",
    )
    Div({ classes("hero-ticker", "burst__dim"); attr("aria-hidden", "true") }) {
        Span({ classes("hero-ticker__label") }) { Text("INTEL") }
        Div({ classes("hero-ticker__viewport") }) {
            Div({ classes("hero-ticker__track") }) {
                repeat(2) {
                    Div({ classes("hero-ticker__group") }) {
                        lines.forEach { Span({ classes("hero-ticker__item") }) { Text(it) } }
                    }
                }
            }
        }
    }
}

/**
 * Case-file pieces fanned out behind the ID card: folder tabs, a partly redacted dossier sheet, a fingerprint
 * card, a violet halftone, clippings and extra stamps.
 */
@Composable
private fun Collage() {
    Div({ classes("collage", "burst__dim"); attr("aria-hidden", "true"); parallax(0.03) }) {
        Layer("collage__folder", rot = -8) {
            Span({ classes("collage__tabs") }) {
                listOf("OP-0001", "HUMINT", "JHB-06").forEach { Span({ classes("collage__tab") }) { Text(it) } }
            }
        }
        Layer("collage__dossier", rot = 9) {
            Span({ classes("collage__dossier-head") }) { Text("SUBJECT FILE // 0001") }
            RedactedLines(
                "Subject operates out of #johannesburg under",
                "callsign #lehlanya. Backend asset, Java and",
                "Kotlin. Contact via #comms only. Handler",
                "notes: #reliable, #mentors #juniors.",
            )
        }
        Layer("collage__halftone")
        Layer("collage__print", rot = -12) {
            Span({ classes("collage__print-ink") })
            Span({ classes("collage__print-label") }) { Text("R. INDEX // LIFTED 02:14") }
        }
        Layer("collage__clip", "collage__clip--a", rot = 5) { Text("Backend operative sighted in JHB") }
        Layer("collage__clip", "collage__clip--b", rot = -4) { Text("Asset still active") }
        Layer("collage__stamp", "collage__stamp--a", rot = -18) { Stamp("Eyes only", StampInk.AMBER, small = true) }
        Layer("collage__stamp", "collage__stamp--b", rot = 13) { Stamp("Copy 02/07", StampInk.STEEL, small = true) }
    }
}

@Composable
private fun IdCard() {
    val op = Intel.operator
    Panel(tag = "aside", accent = Accent.SIGNAL, tilt = Tilt.RIGHT, extraClasses = listOf("idcard-panel"), attrs = { attr("aria-label", "Operator ID card") }) {
        // Tape and stamp belong to the card, not a photo, so they stay put while the prints swap
        Tape("right")
        PanelBar("ID" to "OP-0001", "CLR" to op.clearance)
        Div({ classes("idcard") }) {
            Div({ classes("idcard__photos"); parallax(0.04) }) {
                PolaroidStack(op.photos, label = "operator photo")
                CamStamp("03", "idcard__cam")
                Span({ classes("idcard__verified"); attr("aria-hidden", "true") }) {
                    Stamp("ID verified", StampInk.AMBER, small = true)
                }
            }
            // Overlaps the stack's lower edge so photo and status read as one cluster
            Div({ classes("idcard__status") }) {
                IdRow("Status") { Span({ classes("status-live") }) { Text(op.status) } }
                Beacon()
            }
            Div({ classes("idcard__rows") }) {
                IdRow("Role") { Text("${op.role} // ${op.specialty}") }
                IdRow("AO") { Text(op.location) }
                IdRow("Grid") { Text(op.coordinates) }
            }
        }
        Div({ classes("hero__stamps"); parallax(0.07) }) {
            Stamp("Classified", StampInk.CLASSIFIED, small = true, struck = true)
            Stamp("Declassified", StampInk.AMBER, small = true)
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
    Panel(tag = "aside", accent = Accent.MUTED, tilt = Tilt.LEFT, extraClasses = listOf("panel--steel", "stats"), attrs = { attr("aria-label", "Field statistics") }) {
        PanelBar("AAR" to "FIELD STATS", "REV" to "2026")
        Div({ classes("stats__grid") }) {
            Intel.operator.stats.forEach { s ->
                Div({ classes("stats__cell") }) {
                    Span({ classes("stats__value") }) { if (s.redacted) Redacted(s.value) else Text(s.value) }
                    Span({ classes("stats__label", "readout") }) { Text(s.label) }
                    // Non-breaking spaces glue the brackets to their words, so a wrap never strands a lone "]"
                    if (s.redacted) Deco("redact-hint") { Text("[ HOVER / TAP TO REVEAL ]") }
                }
            }
        }
        Div({ classes("sticker", "stats__sticker"); attr("aria-hidden", "true") }) {
            Span { Text("Now"); Span({ classes("sticker__big") }) { Text("Kotlin") }; Text("training") }
        }
    }
}
