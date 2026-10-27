package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import fyi.lehlanyaverse.components.hud.CamStamp
import fyi.lehlanyaverse.components.hud.Deco
import fyi.lehlanyaverse.components.hud.Layer
import fyi.lehlanyaverse.components.hud.SectionHead
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Time
import fyi.lehlanyaverse.components.hud.spotlight
import fyi.lehlanyaverse.data.FieldNote
import fyi.lehlanyaverse.data.Intel
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.Element
import org.w3c.dom.HTMLDetailsElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.math.atan2
import kotlin.math.sqrt

/** Each exhibit's look on the board, cycled by index so a long list stays a collage. */
private enum class Exhibit(val cls: String) { CARD("evidence--card"), CLIP("evidence--clip"), NOTE("evidence--note") }

/** Scrawled next to each exhibit, cycled by index. Decorative. */
private val annotations = listOf("start here ↘", "this one!!", "ask about it →", "follow the queue", "still digging")

/**
 * 06 // FIELD INTEL as an evidence board, the loudest burst on the page: exhibits pinned to a dark board,
 * joined by red string, with scrawled notes. Each exhibit is a native <details> (works before the app boots):
 * the face is the summary, and opening it lays the full file over the board. Add one by appending to
 * Intel.fieldIntel.
 */
@Composable
fun FieldIntel() {
    Section({ id("field-intel"); attr("aria-labelledby", "intel-title") }) {
        SectionHead("06", "Field intel", "Interests // currently investing time in", "intel-title")
        Div({ classes("board", "burst"); evidenceBoard() }) {
            Deco("burst__spot")
            // Red string, drawn between the pins once the app runs (wide screens); a plain thread on phones
            Deco("board__strings", "burst__dim")
            Ul({ classes("board__items"); attr("role", "list") }) {
                Intel.fieldIntel.forEachIndexed { i, note ->
                    Li({ classes("board__slot") }) {
                        Evidence(note, i)
                        // Beside the <details>, not in it: a closed one renders only its summary
                        Deco("evidence__scrawl") { Text(annotations[i % annotations.size]) }
                    }
                }
            }
            BoardClutter()
        }
    }
}

@Composable
private fun Evidence(note: FieldNote, index: Int) {
    val kind = Exhibit.entries[index % Exhibit.entries.size]
    val letter = ('A' + index).toString()
    TagElement<HTMLDetailsElement>("details", {
        classes("evidence", kind.cls)
        style { property("--r", "${listOf(-3, 2, -1, 3, -2)[index % 5]}deg") }
    }) {
        TagElement<HTMLElement>("summary", { classes("evidence__face"); attr("data-lock", "OPEN FILE") }) {
            Deco("board__pin")
            P({ classes("readout", "evidence__meta") }) {
                Span({ classes("evidence__exhibit") }) { Text("Exhibit $letter") }
                Span { Text("Logged "); Time(note.logged) { Text(note.logged) } }
            }
            H3({ classes("evidence__title") }) { Text(note.title) }
            Span({ classes("evidence__open", "readout") }) {
                Span({ classes("evidence__open-closed") }) { Text("Open file +") }
                Span({ classes("evidence__open-opened") }) { Text("Close file −") }
            }
        }
        Div({ classes("evidence__file") }) {
            P({ classes("readout", "evidence__file-head") }) { Text("FILE // EXHIBIT $letter") }
            P({ classes("evidence__note") }) { Text(note.note) }
            P({ classes("evidence__tags") }) { note.tags.forEach { Span { Text("#$it") } } }
            if (note.url != null) {
                A(note.url, {
                    classes("evidence__src")
                    attr("rel", "noopener noreferrer")
                    attr("aria-label", "Source for: ${note.title}")
                }) { Text("Source →") }
            }
        }
    }
}

/** Decorative extras pinned around the exhibits: a surveillance still, a sticky note, a circled date, stamps. */
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

/**
 * Behaviour for the board: its spotlight, one file open at a time, Esc or a click outside closes it, and red
 * string drawn between the pins (redrawn on resize). Skipped in the export snapshot, so the strings never get baked
 * in.
 */
private fun AttrsScope<*>.evidenceBoard() {
    ref { board ->
        val stopLight = spotlight(board)
        if (AppGlobals.isExporting) return@ref onDispose { stopLight() }
        val strings = board.querySelector(".board__strings") as HTMLElement
        var frame = 0

        fun draw() {
            frame = 0
            strings.innerHTML = ""
            // Phones stack the exhibits; CSS shows a plain thread there instead
            if (!window.matchMedia("(min-width: 64rem)").matches) return
            val origin = board.getBoundingClientRect()
            val pins = board.querySelectorAll(".board__pin").asList().map { (it as Element).getBoundingClientRect() }
                .map { (it.left + it.width / 2 - origin.left) to (it.top + it.height / 2 - origin.top) }
            if (pins.size < 2) return
            // A loop through every pin, plus one cross-link, like a case that keeps coming back to its start
            val links = pins.indices.map { it to (it + 1) % pins.size } + (0 to pins.size / 2)
            links.distinct().forEach { (a, b) ->
                val (x1, y1) = pins[a]
                val (x2, y2) = pins[b]
                val dx = x2 - x1
                val dy = y2 - y1
                val s = document.createElement("span") as HTMLElement
                s.className = "board__string"
                s.style.left = "${x1}px"
                s.style.top = "${y1}px"
                s.style.width = "${sqrt(dx * dx + dy * dy)}px"
                s.style.setProperty("rotate", "${atan2(dy, dx)}rad")
                strings.appendChild(s)
            }
        }

        fun schedule(@Suppress("UNUSED_PARAMETER") e: Event? = null) {
            if (frame == 0) frame = window.requestAnimationFrame { draw() }
        }

        val details = { board.querySelectorAll("details").asList().map { it as HTMLDetailsElement } }
        val onToggle: (Event) -> Unit = { e ->
            val opened = e.target as? HTMLDetailsElement
            if (opened?.open == true) details().filter { it !== opened }.forEach { it.open = false }
        }
        val onKey: (Event) -> Unit = { e ->
            if ((e as KeyboardEvent).key == "Escape") {
                details().firstOrNull { it.open }?.let { open ->
                    open.open = false
                    (open.querySelector("summary") as? HTMLElement)?.focus()
                }
            }
        }
        val onClick: (Event) -> Unit = { e ->
            val t = e.target as? Element
            details().filter { it.open && (t == null || !it.contains(t)) }.forEach { it.open = false }
        }
        val resize: (Event) -> Unit = { schedule() }

        // toggle does not bubble: listen in the capture phase
        board.addEventListener("toggle", onToggle, true)
        document.addEventListener("keydown", onKey)
        document.addEventListener("click", onClick)
        window.addEventListener("resize", resize)
        // Wait two frames for layout (and again once web fonts settle, which shifts the exhibits)
        window.requestAnimationFrame { schedule() }
        document.asDynamic().fonts?.ready?.then { schedule() }

        onDispose {
            stopLight()
            board.removeEventListener("toggle", onToggle, true)
            document.removeEventListener("keydown", onKey)
            document.removeEventListener("click", onClick)
            window.removeEventListener("resize", resize)
            if (frame != 0) window.cancelAnimationFrame(frame)
        }
    }
}
