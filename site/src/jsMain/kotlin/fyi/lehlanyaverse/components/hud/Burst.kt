package fyi.lehlanyaverse.components.hud

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

/*
 * Burst primitives: the collage pieces used only inside burst moments (.burst). All decorative and hidden from
 * screen readers; whatever a piece says is also stated in real text nearby. Styling: hud.css section 8e.
 */

/**
 * One collage piece. [rot] is its rotation in degrees; per-piece opacity comes from `--o` in hud.css.
 */
@Composable
fun Layer(vararg cls: String, rot: Int = 0, content: (@Composable ElementScope<HTMLDivElement>.() -> Unit)? = null) {
    Div({
        classes("layer", *cls)
        attr("aria-hidden", "true")
        if (rot != 0) style { property("--r", "${rot}deg") }
    }, content)
}

/** Lines of fake typescript with some words blacked out: `#word` renders as a solid bar, the rest as ink. */
@Composable
fun RedactedLines(vararg lines: String) {
    lines.forEach { line ->
        Span({ classes("redline") }) {
            line.split(' ').forEach { w ->
                if (w.startsWith('#')) Span({ classes("redline__bar"); style { property("--w", (w.length - 1).toString()) } })
                else Text("$w ")
            }
        }
    }
}
