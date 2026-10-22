package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.data.Commendation
import fyi.lehlanyaverse.data.CommendationKind
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

/** 04 // COMMENDATIONS: certifications and education as medals. */
@Composable
fun Commendations() {
    Section({ id("commendations"); attr("aria-labelledby", "commendations-title") }) {
        SectionHead("04", "Commendations", "Education & certifications // medals", "commendations-title")
        Ul({ classes("medals"); attr("role", "list") }) {
            Intel.commendations.forEach { Li({ classes("medals__item") }) { Medal(it) } }
        }
    }
}

@Composable
private fun Medal(c: Commendation) {
    val cert = c.kind == CommendationKind.CERTIFICATION
    Div({ classes("medal", if (cert) "medal--cert" else "medal--edu") }) {
        Div({ classes("medal__hang"); attr("aria-hidden", "true") }) {
            Span({ classes("medal__ribbon") })
            Span({ classes("medal__badge") }) {
                Span({ classes("medal__year") }) { Text(c.awarded.toString()) }
            }
        }
        Div({ classes("medal__body") }) {
            P({ classes("readout") }) { Text(c.kind.label) }
            H3({ classes("medal__title") }) {
                if (c.credentialUrl != null) {
                    A(c.credentialUrl, { attr("rel", "noopener noreferrer") }) { Text(c.title) }
                } else {
                    Text(c.title)
                }
            }
            P({ classes("medal__issuer") }) { Text(c.issuer) }
            P({ classes("readout") }) {
                Text("Awarded ")
                Span({ classes("hl-acid") }) { Text(c.awarded.toString()) }
                if (c.expires != null) {
                    Text(" · Valid to ")
                    Span({ classes("hl-orange") }) { Text(c.expires.toString()) }
                }
            }
        }
        Deco("medal__corner") { Text(if (cert) "CERT" else "EDU") }
    }
}
