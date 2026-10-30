package fyi.lehlanyaverse.components.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fyi.lehlanyaverse.components.fx.SmoothScroll
import fyi.lehlanyaverse.components.fx.scrollArea
import fyi.lehlanyaverse.components.hud.BtnStyle
import fyi.lehlanyaverse.components.hud.ExtButton
import fyi.lehlanyaverse.components.hud.Stamp
import fyi.lehlanyaverse.components.hud.StampInk
import fyi.lehlanyaverse.components.hud.Time
import fyi.lehlanyaverse.components.hud.monthLabel
import fyi.lehlanyaverse.data.IntelPaper
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

private const val TITLE_ID = "intel-dialog-title"
private const val SUMMARY_ID = "intel-dialog-summary"

/** Longest the close animation may take before the dialog closes anyway. */
private const val CLOSE_FALLBACK_MS = 300

private const val FOCUSABLE = "a[href], button:not([disabled]), [tabindex]:not([tabindex='-1'])"

/**
 * The opened paper: a native modal <dialog> (the page behind goes inert), the paper enlarged and lifted off the
 * board under a spotlight. Esc, CLOSE ✕ and a click on the backdrop close it; Tab stays inside; Lenis stops while
 * it's open so only the paper scrolls. [onClosed] runs once it has fully closed, to hand focus back.
 */
@Composable
internal fun IntelDialog(papers: List<IntelPaper>, openId: String?, onSwitch: (String) -> Unit, onClosed: () -> Unit) {
    var dialog by remember { mutableStateOf<HTMLElement?>(null) }
    val paper = papers.firstOrNull { it.id == openId }

    fun close() {
        val d = dialog ?: return
        if (!(d.asDynamic().open as Boolean) || d.classList.contains("is-closing")) return
        d.classList.add("is-closing")
        val panel = d.querySelector(".intel-file")
        var done = false
        lateinit var onEnd: (Event) -> Unit
        val finish = {
            if (!done) {
                done = true
                panel?.removeEventListener("animationend", onEnd)
                d.classList.remove("is-closing")
                d.asDynamic().close()
            }
        }
        onEnd = { e -> if (e.target === panel) finish() }
        panel?.addEventListener("animationend", onEnd)
        window.setTimeout(finish, CLOSE_FALLBACK_MS)
    }

    // Opens for the first paper, refocuses the title on every switch
    LaunchedEffect(openId, dialog) {
        val d = dialog ?: return@LaunchedEffect
        if (openId == null) return@LaunchedEffect
        if (!(d.asDynamic().open as Boolean)) {
            d.asDynamic().showModal()
            SmoothScroll.lock()
        }
        (d.querySelector(".intel-file__body") as? HTMLElement)?.scrollTop = 0.0
        (d.querySelector("#$TITLE_ID") as? HTMLElement)?.focus()
    }

    DisposableEffect(dialog) {
        val d = dialog ?: return@DisposableEffect onDispose { }
        val onClose: (Event) -> Unit = {
            SmoothScroll.unlock()
            onClosed()
        }
        // Esc: play the close, rather than the browser closing it at once
        val onCancel: (Event) -> Unit = { e -> e.preventDefault(); close() }
        // The backdrop is the dialog element itself; the paper covers the rest
        val onClick: (Event) -> Unit = { e -> if (e.target === d) close() }
        val onKey: (Event) -> Unit = { e -> trapTab(d, e as KeyboardEvent) }
        d.addEventListener("close", onClose)
        d.addEventListener("cancel", onCancel)
        d.addEventListener("click", onClick)
        d.addEventListener("keydown", onKey)
        onDispose {
            d.removeEventListener("close", onClose)
            d.removeEventListener("cancel", onCancel)
            d.removeEventListener("click", onClick)
            d.removeEventListener("keydown", onKey)
            if (d.asDynamic().open as Boolean) {
                d.asDynamic().close()
            }
        }
    }

    TagElement<HTMLElement>("dialog", {
        classes("intel-dialog")
        attr("aria-labelledby", TITLE_ID)
        attr("aria-describedby", SUMMARY_ID)
        ref { el ->
            dialog = el
            onDispose { dialog = null }
        }
    }) {
        // Keyed: switching papers via LINKED INTEL lifts the new one in
        if (paper != null) key(paper.id) { File(paper, papers, onSwitch, ::close) }
    }
}

@Composable
private fun File(paper: IntelPaper, papers: List<IntelPaper>, onSwitch: (String) -> Unit, close: () -> Unit) {
    val index = papers.indexOf(paper)
    Div({
        classes("intel-file", "paper", paperClass(paper.type))
        style { property("--r", "${paper.rotation ?: 0.0}deg") }
    }) {
        Div({ classes("intel-file__body"); scrollArea() }) {
            P({ classes("readout", "paper__meta", "intel-file__meta") }) {
                Span({ classes("paper__exhibit") }) { Text("Exhibit ${'A' + index}") }
                Stamp(paper.type.label, StampInk.AMBER, small = true)
                paper.date?.let { d -> Span { Text("Logged "); Time(d) { Text(monthLabel(d)) } } }
            }
            H2({
                id(TITLE_ID)
                classes("paper__title", "intel-file__title")
                attr("tabindex", "-1")
            }) { Text(paper.title) }
            P({ classes("intel-file__tags", "readout") }) { paper.tags.forEach { Span { Text("#$it") } } }
            P({ id(SUMMARY_ID); classes("intel-file__summary") }) { Text(paper.summary) }
            paper.link?.let {
                Div({ classes("intel-file__actions") }) {
                    ExtButton(
                        it, paper.linkLabel.text, BtnStyle.SIGNAL,
                        ariaLabel = "${paper.linkLabel.text}: ${paper.title} (opens in a new tab)",
                        newTab = true, arrow = "▸",
                    )
                }
            }
            val linked = linkedIds(papers, paper.id).mapNotNull { id -> papers.firstOrNull { it.id == id } }
            if (linked.isNotEmpty()) {
                P({ classes("readout", "intel-file__linked-head"); id("intel-dialog-linked") }) { Text("Linked intel") }
                Ul({ classes("intel-file__linked"); attr("aria-labelledby", "intel-dialog-linked") }) {
                    linked.forEach { other ->
                        Li {
                            Button({
                                classes("chip", "intel-file__chip")
                                attr("type", "button")
                                onClick { onSwitch(other.id) }
                            }) { Text(other.title) }
                        }
                    }
                }
            }
        }
        Button({
            classes("btn", "intel-file__close")
            attr("type", "button")
            attr("aria-label", "Close file")
            onClick { close() }
        }) { Text("Close ✕") }
    }
}

/** Keeps Tab and Shift+Tab inside [d]: showModal alone lets focus out to the browser's own controls. */
private fun trapTab(d: HTMLElement, e: KeyboardEvent) {
    if (e.key != "Tab") return
    val focusables = d.querySelectorAll(FOCUSABLE).asList().map { it as HTMLElement }
    if (focusables.isEmpty()) return
    val active = d.ownerDocument?.activeElement
    val first = focusables.first()
    val last = focusables.last()
    val outside = active == null || !focusables.contains(active)
    when {
        e.shiftKey && (active === first || outside) -> { e.preventDefault(); last.focus() }
        !e.shiftKey && (active === last) -> { e.preventDefault(); first.focus() }
        !e.shiftKey && outside -> { e.preventDefault(); first.focus() }
    }
}
