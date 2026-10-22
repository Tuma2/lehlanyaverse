package fyi.lehlanyaverse.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fyi.lehlanyaverse.data.Intel
import fyi.lehlanyaverse.data.OpStatus
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/** Ticker lines are derived from Intel, so they stay current without editing this file. */
private fun tickerItems(): List<String> {
    val op = Intel.operator
    val deployed = Intel.operations.count { it.status == OpStatus.DEPLOYED }
    val active = Intel.operations.count { it.status == OpStatus.IN_PROGRESS }
    return listOf(
        "Status: ${op.status}",
        "Operator: ${op.name}",
        "AO: ${op.location}",
        "Primary: ${Intel.loadout.primary.take(2).joinToString(" + ") { it.name }}",
        "Now training: ${Intel.loadout.secondary.joinToString(" + ") { it.name }}",
        "Ops deployed: $deployed",
        "Ops in progress: $active",
        "Grid ${op.coordinates}",
    )
}

/**
 * Marquee ticker. Pauses on hover/focus and has an explicit pause control (WCAG 2.2.2).
 * The duplicate copy that makes the loop seamless is hidden from assistive tech.
 */
@Composable
fun Ticker() {
    var paused by remember { mutableStateOf(false) }
    val items = tickerItems()
    Div({
        classes("ticker")
        attr("role", "region")
        attr("aria-label", "Status ticker")
        if (paused) attr("data-paused", "")
    }) {
        Span({ classes("ticker__label") }) { Text("LIVE FEED") }
        Div({ classes("ticker__viewport") }) {
            Div({ classes("ticker__track") }) {
                repeat(2) { copy ->
                    Div({ classes("ticker__group"); if (copy == 1) attr("aria-hidden", "true") }) {
                        items.forEach { Span({ classes("ticker__item") }) { Text(it) } }
                    }
                }
            }
        }
        Button({
            classes("ticker__btn")
            attr("aria-pressed", paused.toString())
            attr("aria-label", "Pause ticker")
            onClick { paused = !paused }
        }) {
            Span({ attr("aria-hidden", "true") }) { Text(if (paused) "▶" else "❚❚") }
        }
    }
}
