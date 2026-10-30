package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fyi.lehlanyaverse.components.fx.Fx
import fyi.lehlanyaverse.components.hud.CamStamp
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Time
import fyi.lehlanyaverse.components.hud.armOnEntry
import fyi.lehlanyaverse.components.hud.monthLabel
import fyi.lehlanyaverse.components.hud.spotlight
import fyi.lehlanyaverse.data.IntelPaper
import fyi.lehlanyaverse.data.IntelType
import fyi.lehlanyaverse.data.Intel
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

private const val SVG_NS = "http://www.w3.org/2000/svg"

/** Scrawled beside each paper, cycled by index. Decorative. */
private val annotations = listOf("start here ↘", "this one!!", "ask about it →", "follow the queue", "still digging")

/** The CSS class for a paper's look, e.g. `paper--index-card`. */
internal fun paperClass(type: IntelType) = "paper--" + type.name.lowercase().replace('_', '-')

/**
 * 06 // FIELD INTEL as an evidence board, the loudest burst on the page: papers pinned where Intel.fieldIntel puts
 * them, red string between linked topics, and the full file in a pop-up (IntelDialog.kt). Hovering or focusing a
 * paper traces its string. Below 64rem the same papers stack as pinned cards, with their links as chips. It's all
 * laid out in % (BoardLayout.kt), so the export snapshot is the finished board. Add a paper in Intel.kt.
 */
@Composable
fun FieldIntel() {
    val papers = Intel.fieldIntel
    val layout = remember(papers) { layoutBoard(papers) }
    var traced by remember { mutableStateOf<String?>(null) }
    var openId by remember { mutableStateOf<String?>(null) }
    var opener by remember { mutableStateOf<HTMLElement?>(null) }
    val hot = traced?.let { linkedIds(papers, it).toSet() + it }.orEmpty()

    Section({ id("field-intel"); attr("aria-labelledby", "intel-title") }) {
        SectionHead("06", "Field intel", "Interests // currently investing time in", "intel-title")
        Div({
            classes("board", "burst")
            if (traced != null) classes("is-tracing")
            ref { el ->
                val stopLight = spotlight(el)
                // One fade as the board first comes into view (off without motion, and if it's already on screen)
                val unarm = if (Fx.motion) armOnEntry(el) else ({})
                onDispose { stopLight(); unarm() }
            }
        }) {
            Deco("burst__spot")
            Div({ classes("board__surface"); style { property("--rows", layout.rows) } }) {
                Strings(layout.wires, traced)
                layout.papers.forEach { p ->
                    Paper(
                        p,
                        linked = linkedIds(papers, p.paper.id).mapNotNull { id -> papers.firstOrNull { it.id == id }?.title },
                        state = when {
                            traced == null -> null
                            p.paper.id == traced -> "is-traced"
                            p.paper.id in hot -> "is-linked"
                            else -> null
                        },
                        onTrace = { traced = it },
                        onOpen = { el -> opener = el; openId = p.paper.id },
                    )
                }
                // Pins over everything, string and overlapping papers included
                Div({ classes("board__pins"); attr("aria-hidden", "true") }) {
                    layout.papers.forEach { p ->
                        Span({
                            classes("board__pin")
                            if (linkedIds(papers, p.paper.id).isEmpty()) classes("board__pin--loose")
                            if (p.paper.id in hot) classes("is-linked")
                            style { property("left", "${p.x}%"); property("top", "${p.y}%") }
                        })
                    }
                }
            }
            BoardClutter()
        }
        IntelDialog(papers, openId, onSwitch = { openId = it }, onClosed = {
            openId = null
            opener?.focus()
        })
    }
}

@Composable
private fun Paper(p: Placed, linked: List<String>, state: String?, onTrace: (String?) -> Unit, onOpen: (HTMLElement) -> Unit) {
    val paper = p.paper
    Button({
        classes("paper", paperClass(paper.type), "board__paper")
        if (linked.isEmpty()) classes("board__paper--loose")
        state?.let { classes(it) }
        attr("type", "button")
        attr("aria-label", "Open intel: ${paper.title}")
        attr("aria-haspopup", "dialog")
        attr("data-lock", "OPEN FILE")
        style {
            property("--x", "${p.x}%")
            property("--y", "${p.y}%")
            property("--r", "${p.rot}deg")
        }
        onClick { e -> onOpen(e.currentTarget as HTMLElement) }
        onMouseEnter { onTrace(paper.id) }
        onMouseLeave { onTrace(null) }
        onFocusIn { onTrace(paper.id) }
        onFocusOut { onTrace(null) }
    }) {
        if (paper.type == IntelType.PHOTO) {
            Span({ classes("paper__img") }) { CamStamp("${p.index + 1}".padStart(2, '0'), "paper__cam") }
        }
        Span({ classes("readout", "paper__meta") }) {
            Span({ classes("paper__exhibit") }) { Text("Exhibit ${'A' + p.index}") }
            paper.date?.let { d -> Span { Text("Logged "); Time(d) { Text(monthLabel(d)) } } }
        }
        Span({ classes("paper__title") }) { Text(paper.title) }
        Span({ classes("paper__teaser") }) { Text(paper.teaser) }
        Span({ classes("paper__open", "readout") }) { Text("Open file +") }
        if (linked.isNotEmpty()) {
            Span({ classes("paper__links") }) {
                Span({ classes("readout", "paper__links-head") }) { Text("Linked to:") }
                linked.forEach { Span({ classes("paper__chip") }) { Text(it) } }
            }
        }
        // "start here" only on the first paper; the rest cycle through what's left
        val scrawl = if (p.index == 0) annotations[0] else annotations[1 + (p.index - 1) % (annotations.size - 1)]
        Deco("paper__scrawl") { Text(if (linked.isEmpty()) "loose lead" else scrawl) }
    }
}

/**
 * Red string, one path per linked pair, drawn in the board's 0..100 box (stretched to fit, the stroke kept true by
 * `vector-effect`). Built with the SVG namespace by hand: Compose HTML only makes HTML elements.
 */
@Composable
private fun Strings(wires: List<Wire>, traced: String?) {
    var paths by remember { mutableStateOf<List<Pair<Wire, Element>>>(emptyList()) }
    Span({
        classes("board__strings", "burst__dim")
        attr("aria-hidden", "true")
        ref { el ->
            val svg = document.createElementNS(SVG_NS, "svg")
            svg.setAttribute("viewBox", "0 0 100 100")
            svg.setAttribute("preserveAspectRatio", "none")
            // Each string over a wider, fainter copy of itself that shows only while traced: a glow that doesn't
            // stretch with the box, as a filter would
            paths = wires.flatMap { w ->
                listOf("board__glow", null).map { cls ->
                    val path = document.createElementNS(SVG_NS, "path")
                    path.setAttribute("d", w.d)
                    path.setAttribute("data-ends", "${w.a} ${w.b}")
                    cls?.let { path.classList.add(it) }
                    svg.appendChild(path)
                    w to path
                }
            }
            el.appendChild(svg)
            onDispose { svg.remove(); paths = emptyList() }
        }
    })
    DisposableEffect(traced, paths) {
        paths.forEach { (w, path) -> path.classList.toggle("is-hot", traced != null && (w.a == traced || w.b == traced)) }
        onDispose { }
    }
}

/** Decorative extras pinned around the papers: a surveillance still, a sticky note, a circled date, stamps. */
@Composable
private fun BoardClutter() {
    Layer("board__still", rot = 4) {
        Deco("board__pin")
        Span({ classes("board__still-img") })
        CamStamp("07", "board__still-cam")
        Span({ classes("board__still-cap") }) { Text("AO // JHB · SUBJECT ACTIVE") }
    }
    Layer("board__sticky", rot = -7) {
        Deco("board__pin")
        Text("who handles the dead letters?")
    }
    Layer("board__circle", rot = -10) { Text("2026") }
    Layer("board__arrow", rot = 18) { Text("⟶") }
    Layer("board__stamp", rot = -14) { Stamp("Under review", StampInk.AMBER, small = true) }
    Layer("board__stamp", "board__stamp--b", rot = 8) { Stamp("Classified", StampInk.CLASSIFIED, small = true) }
}
