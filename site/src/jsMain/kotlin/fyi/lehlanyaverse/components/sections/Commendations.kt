package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Todo
import fyi.lehlanyaverse.components.hud.litOnView
import fyi.lehlanyaverse.data.Commendation
import fyi.lehlanyaverse.data.CommendationGroup
import fyi.lehlanyaverse.data.CommendationKind
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

/** How a medal hangs on the wall, cycled by index: hex badge, embossed seal, lanyard ID tag. */
private val mounts = listOf("medal--badge", "medal--seal", "medal--tag")

/** Small, varied tilts so the wall looks hung by hand. */
private val tilts = listOf(-3, 2, -1, 3, -2, 1)

/**
 * 04 // COMMENDATIONS as a medal/ID wall (a burst): one cluster per [CommendationGroup], each medal hung at its
 * own angle. The SECURITY group is framed as the "Security clearance" cluster, an ID on a lanyard with a
 * clearance stamp. Titles and issuers stay real text on solid plates, so they read first.
 */
@Composable
fun Commendations() {
    Section({ id("commendations"); attr("aria-labelledby", "commendations-title") }) {
        SectionHead("04", "Commendations", "Education & certifications // medals", "commendations-title")
        Div({ classes("wall", "burst"); litOnView() }) {
            Deco("burst__spot")
            val byGroup = Intel.commendations.groupBy { it.group }
            CommendationGroup.entries.forEach { group ->
                val items = byGroup[group] ?: return@forEach
                if (group.isClearance) ClearanceCluster(group, items) else WallGroup(group, items)
            }
            // Hung in the empty wall space beside the shorter clusters (wide screens)
            Layer("wall__seal", rot = -8) { Span { Text("On record") } }
            Layer("wall__stamp", rot = 12) { Stamp("Verified", StampInk.AMBER) }
        }
    }
}

@Composable
private fun ClearanceCluster(group: CommendationGroup, items: List<Commendation>) {
    val titleId = "commendations-${group.name.lowercase()}"
    Div({ classes("medal-group", "clearance") }) {
        Deco("clearance__lanyard")
        Deco("clearance__clip")
        Div({ classes("clearance__head") }) {
            H3({ id(titleId); classes("clearance__title") }) {
                Text("Security clearance")
                Span({ classes("sr-only") }) { Text(": ${group.label}") }
            }
            P({ classes("readout", "clearance__sub") }) {
                Text("${group.label} // ${items.size.toString().padStart(2, '0')} on file")
            }
        }
        Layer("clearance__stamp", rot = -9) { Stamp("Clearance granted", StampInk.AMBER) }
        Ul({ classes("medals"); attr("role", "list"); attr("aria-labelledby", titleId) }) {
            items.forEachIndexed { i, c -> Li({ classes("medals__item") }) { Medal(c, i) } }
        }
    }
}

@Composable
private fun WallGroup(group: CommendationGroup, items: List<Commendation>) {
    val titleId = "commendations-${group.name.lowercase()}"
    Div({ classes("medal-group") }) {
        H3({ id(titleId); classes("medal-group__title", "readout") }) {
            Text(group.label)
            Deco("medal-group__count") { Text(" // ${items.size.toString().padStart(2, '0')}") }
        }
        Ul({ classes("medals"); attr("role", "list"); attr("aria-labelledby", titleId) }) {
            // Offset the cycle per group so neighbouring clusters don't hang identically
            items.forEachIndexed { i, c -> Li({ classes("medals__item") }) { Medal(c, i + group.ordinal) } }
        }
    }
}

@Composable
private fun Medal(c: Commendation, index: Int) {
    val edu = c.kind == CommendationKind.EDUCATION
    Div({
        classes("medal", if (edu) "medal--edu" else "medal--cert", mounts[index % mounts.size])
        style { property("--r", "${tilts[index % tilts.size]}deg") }
    }) {
        Div({ classes("medal__hang"); attr("aria-hidden", "true") }) {
            Span({ classes("medal__ribbon") })
            Span({ classes("medal__badge") }) {
                Span({ classes("medal__year") }) { Text(c.awarded?.toString() ?: "----") }
            }
        }
        Div({ classes("medal__body") }) {
            P({ classes("readout") }) { Text(c.kind.label) }
            H4({ classes("medal__title") }) {
                if (c.credentialUrl != null) {
                    A(c.credentialUrl, { attr("rel", "noopener noreferrer") }) { Text(c.title) }
                } else {
                    Text(c.title)
                }
            }
            P({ classes("medal__issuer") }) { if (c.issuer != null) Text(c.issuer) else Todo("issuer") }
            P({ classes("readout") }) {
                Text("${c.kind.dated} ")
                if (c.awarded != null) Span({ classes("hl-ink") }) { Text(c.awarded.toString()) } else Todo("year")
                if (c.expires != null) {
                    Text(" · Valid to ")
                    Span({ classes("hl-ink") }) { Text(c.expires.toString()) }
                }
            }
        }
        Deco("medal__corner") { Text(c.kind.corner) }
    }
}
