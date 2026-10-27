package fyi.lehlanyaverse.components.fx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.layout.dossierSections
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/** Lead time before the scroll starts: the flash is ~300ms and the glide begins as it fades. */
private const val LEAD_MS = 220

/**
 * "DEPLOYING TO SECTOR 03 // SERVICE RECORD": a one-line overlay with a scanline sweep, a green/violet split and a
 * DEPLOYED stamp, shown for an instant before a nav or tracker jump. Decorative (focus moves to the section on
 * arrival), so hidden from screen readers.
 */
object DeployFlash {
    private var line by mutableStateOf<String?>(null)
    private var shot by mutableStateOf(0)

    /** Shows the flash for the section [id], then runs [then]. Without motion, runs [then] straight away. */
    fun play(id: String, then: () -> Unit) {
        if (!Fx.DEPLOY_FLASH || !Fx.motion) return then()
        val i = dossierSections.indexOfFirst { it.first == id }
        line = if (i >= 0) "DEPLOYING TO SECTOR ${(i + 1).toString().padStart(2, '0')} // ${dossierSections[i].second.uppercase()}"
        else "DEPLOYING // ${id.uppercase().replace('-', ' ')}"
        shot++
        window.setTimeout({ then() }, LEAD_MS)
    }

    @Composable
    fun Overlay() {
        val text = line ?: return
        // A fresh element per shot restarts the CSS animation without forcing a reflow
        key(shot) {
            Div({ classes("deploy-flash"); attr("aria-hidden", "true") }) {
                // Green and violet halves slide apart behind the line; DEPLOYED is stamped under it
                Span({ classes("deploy-flash__split") })
                Span({ classes("deploy-flash__line") }) {
                    Span({ classes("deploy-flash__chev") }) { Text("▶▶ ") }
                    Text(text)
                }
                Span({ classes("deploy-flash__stamp") }) { Stamp("Deployed", small = true) }
            }
        }
    }
}
