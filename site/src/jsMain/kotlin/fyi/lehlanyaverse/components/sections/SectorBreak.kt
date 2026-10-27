package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.fx.parallax
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.litOnView
import fyi.lehlanyaverse.components.layout.dossierSections
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/** The break's big word: a codeword for the sector, so it never just repeats the heading that follows. */
private val codewords = listOf("Dossier", "Armoury", "Mission log", "Honours", "Live ops", "Evidence", "Uplink")

/**
 * Burst strip between two sectors: an oversized sector number, clashing type, stamps, a violet flash and two
 * counter-scrolling marquees of redacted text, then straight back to dark calm. Entirely decorative (the real
 * section heading follows), so hidden from screen readers. [sectorIndex] is 0-based into [dossierSections].
 */
@Composable
fun SectorBreak(sectorIndex: Int) {
    val title = dossierSections[sectorIndex].second
    val n = (sectorIndex + 1).toString().padStart(2, '0')
    Div({ classes("sector-break", "burst"); attr("aria-hidden", "true"); litOnView() }) {
        Deco("burst__spot")
        Marquee(n, title, reverse = false)
        Div({ classes("sector-break__stage", "burst__dim") }) {
            Layer("sector-break__flash", rot = -6)
            Span({ classes("sector-break__num"); parallax(0.06) }) { Text(n) }
            Div({ classes("sector-break__words") }) {
                Span({ classes("sector-break__enter") }) { Text("Now entering") }
                Span({ classes("sector-break__title") }) { Text(codewords.getOrElse(sectorIndex) { title }) }
                Span({ classes("sector-break__meta") }) { Text("SECTOR $n // ${title.uppercase()} // CLEARANCE GRANTED") }
            }
            Layer("sector-break__stamp", "sector-break__stamp--a", rot = -12) { Stamp("Sector $n", StampInk.AMBER, small = true) }
            Layer("sector-break__stamp", "sector-break__stamp--b", rot = 9) {
                Stamp(if (sectorIndex % 2 == 0) "Eyes only" else "Need to know", StampInk.CLASSIFIED, small = true)
            }
        }
        Marquee(n, title, reverse = true)
    }
}

/** One row of mono "intercepted" text with blacked-out words; two copies make the loop seamless. */
@Composable
private fun Marquee(n: String, title: String, reverse: Boolean) {
    val words = listOf(
        "INTERCEPT $n", "#########", title.uppercase(), "████", "ROUTE VIA JHB-0$n", "######", "SIGNAL CLEAN",
        "##########", "HANDLER ACK", "#####", "PKT 0x3F${n}A", "########",
    ).let { if (reverse) it.reversed() else it }
    Div({ classes("sector-break__marquee", if (reverse) "sector-break__marquee--rev" else "sector-break__marquee--fwd") }) {
        Div({ classes("sector-break__track") }) {
            repeat(2) {
                Span({ classes("sector-break__group") }) {
                    words.forEach { w ->
                        if (w.startsWith('#')) Span({ classes("redline__bar"); style { property("--w", w.length.toString()) } })
                        else Span { Text(w) }
                    }
                }
            }
        }
    }
}
