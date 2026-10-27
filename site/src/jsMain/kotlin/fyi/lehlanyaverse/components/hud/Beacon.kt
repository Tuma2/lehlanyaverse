package fyi.lehlanyaverse.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fyi.lehlanyaverse.data.Intel
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/**
 * Availability beacon, driven by [fyi.lehlanyaverse.data.Operator.openToOpportunities]. Open: a pulsing signal dot,
 * "OPEN TO OPPORTUNITIES" and a tooltip with [fyi.lehlanyaverse.data.Operator.availabilityNote]. Otherwise a dim,
 * static dot reading "ON ASSIGNMENT". The status is always spelled out, never colour alone.
 *
 * Links to COMMS (SmoothScroll glides same-page hash links). [compact] is the header version: label only, and a
 * shorter label on phones. The tooltip shows on hover / focus, stays while hovered and Esc dismisses it (WCAG 1.4.13).
 */
@Composable
fun Beacon(compact: Boolean = false) {
    val op = Intel.operator
    val open = op.openToOpportunities
    val tipId = if (compact) "beacon-tip-hdr" else "beacon-tip"
    var tipDismissed by remember { mutableStateOf(false) }

    Span({
        classes(*listOfNotNull("beacon", "beacon--compact".takeIf { compact }, "beacon--off".takeIf { !open }).toTypedArray())
        if (tipDismissed) attr("data-tip-hidden", "")
        onMouseLeave { tipDismissed = false }
        onFocusOut { tipDismissed = false }
    }) {
        A("/#comms", {
            classes("beacon__link")
            attr("data-lock", "OPEN COMMS") // label for the cursor reticle (fx/Reticle.kt)
            if (open) attr("aria-describedby", tipId)
            onKeyDown { if (it.key == "Escape") tipDismissed = true }
        }) {
            Deco("beacon__dot")
            Span({ classes("beacon__text") }) {
                val label = if (open) "OPEN TO OPPORTUNITIES" else "ON ASSIGNMENT"
                if (compact) {
                    // Phones get the short form; screen readers always hear the full one
                    Span({ classes("beacon__label", "beacon__label--full") }) { Text(label) }
                    Deco("beacon__label", "beacon__label--short") { Text(if (open) "OPEN" else "ENGAGED") }
                    Span({ classes("sr-only") }) { Text(" — open comms") }
                } else {
                    Span({ classes("beacon__label") }) { Text(label) }
                    if (open) Span({ classes("beacon__sub") }) { Text("AVAILABLE FOR NEW ASSIGNMENT") }
                }
            }
        }
        if (open) Span({ id(tipId); classes("beacon__tip"); attr("role", "tooltip") }) { Text(op.availabilityNote) }
    }
}
