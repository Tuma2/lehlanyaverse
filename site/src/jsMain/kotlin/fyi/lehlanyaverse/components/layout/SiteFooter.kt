package fyi.lehlanyaverse.components.layout

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Footer
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date

@Composable
fun SiteFooter() {
    val op = Intel.operator
    Footer({ classes("site-footer") }) {
        Div({ classes("wrap", "site-footer__inner") }) {
            P({ classes("site-footer__end"); attr("aria-hidden", "true") }) {
                Text("END OF DOSSIER")
                Span { Text(" //") }
            }
            P { Text("© ${Date().getFullYear()} ${op.name}") }
            P { Text("${op.location} · ${op.coordinates}") }
            P { Text("Built in Kotlin with Kobweb · No trackers") }
        }
    }
}
