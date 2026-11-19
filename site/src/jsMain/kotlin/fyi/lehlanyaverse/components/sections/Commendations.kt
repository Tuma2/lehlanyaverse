package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Todo
import fyi.lehlanyaverse.components.hud.litOnView
import fyi.lehlanyaverse.data.AssessmentKind
import fyi.lehlanyaverse.data.Commendation
import fyi.lehlanyaverse.data.SkillAssessment
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
                // Field assessments hang right after the clearance cluster, before Cloud & Engineering
                if (group.isClearance && Intel.skillAssessments.isNotEmpty()) FieldAssessments(Intel.skillAssessments)
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

/**
 * FIELD ASSESSMENTS // HACKERRANK: third-party skill assessments as lanyard ID cards in the wall's own style.
 * The issuer is named in text only (no logo, brand colours or badge art). Missing data renders as absence:
 * no level stamp without a level, no VERIFY link without a URL, no "View profile" without a profile URL.
 */
@Composable
private fun FieldAssessments(items: List<SkillAssessment>) {
    val titleId = "commendations-assessments"
    val issuer = items.first().issuer
    Div({ classes("medal-group", "assessments") }) {
        Div({ classes("assessments__head") }) {
            H3({ id(titleId); classes("medal-group__title", "readout") }) {
                Text("Field assessments // $issuer")
                Deco("medal-group__count") { Text(" // ${items.size.toString().padStart(2, '0')}") }
            }
            Intel.hackerRankProfileUrl?.let { url ->
                A(url, {
                    classes("hud-link", "assessments__profile")
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    attr("aria-label", "View $issuer profile (opens in new tab)")
                    attr("data-lock", "OPEN FILE")
                }) {
                    Text("View profile")
                    Span({ attr("aria-hidden", "true") }) { Text(" ▸") }
                }
            }
        }
        Ul({ classes("medals"); attr("role", "list"); attr("aria-labelledby", titleId) }) {
            items.forEachIndexed { i, a -> Li({ classes("medals__item") }) { AssessmentCard(a, i) } }
        }
    }
}

@Composable
private fun AssessmentCard(a: SkillAssessment, index: Int) {
    Div({
        classes("medal", "medal--cert", "medal--tag", "medal--assess")
        style { property("--r", "${tilts[(index + 1) % tilts.size]}deg") }
    }) {
        Div({ classes("medal__hang"); attr("aria-hidden", "true") }) {
            Span({ classes("medal__ribbon") })
            Span({ classes("medal__badge") }) { Span({ classes("medal__year", "medal__code") }) { Text(shortCode(a.name)) } }
        }
        Div({ classes("medal__body") }) {
            P({ classes("readout") }) { Text("Skill ${a.kind.noun}") }
            H4({ classes("medal__title") }) { Text(a.name) }
            if (a.kind == AssessmentKind.BADGE && a.stars != null) {
                P({ classes("medal__stars") }) {
                    Deco { Text("★".repeat(a.stars) + "☆".repeat(5 - a.stars)) }
                    Span({ classes("sr-only") }) { Text("${a.stars} of 5 stars") }
                }
            } else if (a.level != null) {
                // Stamps are decorative (aria-hidden); the level is also in the readout line below
                Div({ classes("medal__stamp") }) { Stamp(a.level.label, StampInk.SIGNAL, small = true) }
            }
            P({ classes("readout", "medal__assessed") }) {
                Text("Assessed // ${a.issuer}")
                if (a.level != null && a.kind == AssessmentKind.CERTIFICATE) {
                    Text(" · ")
                    Span({ classes("hl-ink") }) { Text(a.level.label) }
                }
            }
            a.verifyUrl?.let { url ->
                A(url, {
                    classes("hud-link", "medal__verify")
                    attr("target", "_blank")
                    attr("rel", "noopener noreferrer")
                    attr("aria-label", a.verifyLabel)
                    attr("data-lock", "VERIFY")
                }) {
                    Text("Verify")
                    Span({ attr("aria-hidden", "true") }) { Text(" ▸") }
                }
            }
        }
        Deco("medal__corner") { Text("ASSESS") }
    }
}

/** Badge code: initials for several words ("PS"), short words whole ("SQL", "JAVA"), else two letters ("PY"). */
private fun shortCode(name: String): String {
    val words = name.split(' ', '-').filter { it.isNotBlank() }
    return when {
        words.size > 1 -> words.joinToString("") { it.first().uppercase() }
        name.length <= 4 -> name.uppercase()
        else -> name.take(2).uppercase()
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
