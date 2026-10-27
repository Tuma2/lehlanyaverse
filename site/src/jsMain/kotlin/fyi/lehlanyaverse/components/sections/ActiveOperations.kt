package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.navigation.Anchor
import fyi.lehlanyaverse.components.hud.Accent
import fyi.lehlanyaverse.components.hud.Chips
import fyi.lehlanyaverse.components.hud.DeclassStamp
import fyi.lehlanyaverse.components.hud.OpLinks
import fyi.lehlanyaverse.components.hud.Panel
import fyi.lehlanyaverse.components.hud.PanelBar
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.StatusTag
import fyi.lehlanyaverse.components.hud.Tilt
import fyi.lehlanyaverse.components.hud.declassLine
import fyi.lehlanyaverse.components.hud.declassify
import fyi.lehlanyaverse.data.Intel
import fyi.lehlanyaverse.data.OpStatus
import fyi.lehlanyaverse.data.Operation
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

/** 05 // ACTIVE OPERATIONS: projects. Each card links to its own debrief page at /operations/{slug}. */
@Composable
fun ActiveOperations() {
    Section({ id("operations"); attr("aria-labelledby", "operations-title") }) {
        SectionHead("05", "Active operations", "Projects // current deployments", "operations-title")
        Ul({ classes("ops"); attr("role", "list") }) {
            Intel.operations.forEachIndexed { i, op -> Li { OpCard(op, i) } }
        }
    }
}

@Composable
private fun OpCard(op: Operation, index: Int) {
    val titleId = "op-${op.slug}"
    Panel(
        tag = "article",
        accent = when (op.status) {
            OpStatus.DEPLOYED -> Accent.SIGNAL
            OpStatus.IN_PROGRESS -> Accent.STEEL
            OpStatus.RECON -> Accent.MUTED
        },
        tilt = if (index % 2 == 0) Tilt.LEFT else Tilt.RIGHT,
        extraClasses = listOf("op"),
        attrs = { attr("aria-labelledby", titleId); declassify() },
    ) {
        PanelBar("OP" to (index + 1).toString().padStart(2, '0'), "FILE" to op.slug.uppercase())
        Div({ classes("op__head") }) {
            P({ classes("op__codename", "readout"); declassLine(0) }) { Text(op.codename) }
            StatusTag(op.status)
        }
        H3({ id(titleId); classes("op__title"); declassLine(1) }) {
            Anchor("/operations/${op.slug}", { classes("op__link") }) { Text(op.title) }
        }
        P({ classes("op__summary") }) { Text(op.summary) }
        Chips(op.stack, "${op.title} stack")
        Div({ classes("op__actions") }) {
            Anchor("/operations/${op.slug}", {
                classes("btn", "btn--solid")
                attr("aria-label", "View debrief: ${op.title}")
            }) {
                Text("View debrief")
                Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("→") }
            }
            OpLinks(op)
        }
        DeclassStamp()
    }
}
