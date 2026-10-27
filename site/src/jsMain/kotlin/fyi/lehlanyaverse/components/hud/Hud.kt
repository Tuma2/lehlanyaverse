package fyi.lehlanyaverse.components.hud

import androidx.compose.runtime.Composable
import fyi.lehlanyaverse.components.fx.Fx
import fyi.lehlanyaverse.components.fx.parallax
import fyi.lehlanyaverse.data.OpStatus
import fyi.lehlanyaverse.data.Operation
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

/*
 * HUD primitives. Each one is a thin wrapper over semantic HTML; all styling lives in public/css/hud.css.
 */

enum class Accent(val cls: String?) { STEEL(null), SIGNAL("panel--signal"), INK("panel--ink"), MUTED("panel--muted") }

enum class Tilt(val cls: String?) { NONE(null), LEFT("tilt-l"), RIGHT("tilt-r"), HARD_RIGHT("tilt-rr") }

/** Panel framed by corner brackets. [tag] lets it render as `article`, `section`, `aside`… for correct semantics. */
@Composable
fun Panel(
    tag: String = "div",
    accent: Accent = Accent.STEEL,
    tilt: Tilt = Tilt.NONE,
    extraClasses: List<String> = emptyList(),
    attrs: (AttrsScope<HTMLElement>.() -> Unit)? = null,
    content: ContentBuilder<HTMLElement>,
) {
    TagElement<HTMLElement>(tag, {
        classes(*(listOfNotNull("panel", accent.cls, tilt.cls) + extraClasses).toTypedArray())
        attrs?.invoke(this)
    }, content)
}

/** The ID / timestamp strip across the top of a panel. Decorative metadata, so hidden from screen readers. */
@Composable
fun PanelBar(vararg cells: Pair<String, String>) {
    Div({ classes("panel__bar", "readout"); attr("aria-hidden", "true") }) {
        cells.forEach { (k, v) ->
            Span { Text("$k "); TagElement<HTMLElement>("b", null) { Text(v) } }
        }
    }
}

/** Big outlined section number + condensed title. The `<h2>` carries [titleId] for `aria-labelledby`. */
@Composable
fun SectionHead(number: String, title: String, kicker: String, titleId: String) {
    Div({ classes("sec-head") }) {
        // Spans the full width above the number: it is too long to sit beside it on phones
        if (Fx.PROJECTED_HEADERS) SectorLine("SECTOR $number // ${title.uppercase()}")
        Span({ classes("sec-head__num"); attr("aria-hidden", "true") }) { Text(number) }
        Div {
            Span({ classes("sec-head__kicker", "readout") }) { Text(kicker) }
            H2({ id(titleId); classes("sec-head__title") }) { Text(title) }
        }
    }
}

/**
 * "SECTOR 03 // SERVICE RECORD" above a section title, projected onto the surface: it glows, leans back and
 * flickers on (with the title) when the section first scrolls into view; otherwise (no JS, reduced motion,
 * already on screen) it is simply there. Repeats the heading, so hidden from screen readers.
 */
@Composable
private fun SectorLine(text: String) {
    Span({
        classes("sec-head__sector")
        attr("aria-hidden", "true")
        revealOnEntry()
    }) {
        Span({ classes("sec-head__sector-text") }) { Text(text) }
    }
}

/** Amber for stamps by default; signal for live states only; red for CLASSIFIED only. */
enum class StampInk(val cls: String?) { AMBER(null), SIGNAL("stamp--signal"), STEEL("stamp--steel"), CLASSIFIED("stamp--classified") }

/** Rubber stamp. Decorative: anything a stamp says must also be stated in real text nearby. */
@Composable
fun Stamp(text: String, ink: StampInk = StampInk.AMBER, small: Boolean = false, struck: Boolean = false) {
    Span({
        classes(*listOfNotNull("stamp", ink.cls, "stamp--sm".takeIf { small }, "stamp--void".takeIf { struck }).toTypedArray())
        attr("aria-hidden", "true")
    }) { Text(text) }
}

@Composable
fun Tape(variant: String? = null) {
    Span({ classes(*listOfNotNull("tape", variant?.let { "tape--$it" }).toTypedArray()); attr("aria-hidden", "true") })
}

@Composable
fun Hazard() {
    Div({ classes("hazard"); attr("aria-hidden", "true") })
}

/**
 * Redacted text: a black bar that declassifies on hover or keyboard focus.
 * The real text is always in the DOM, so assistive tech reads it normally.
 */
@Composable
fun Redacted(text: String) {
    Span({
        classes("redact")
        tabIndex(0)
        attr("title", "Declassify")
        // The cursor reticle labels what it locks onto; this keeps it from reading out the hidden text
        attr("data-lock", "DECLASSIFY")
    }) { Text(text) }
}

/** Single-line typewriter readout. Width is measured in `ch`, so keep it to short mono strings. */
@Composable
fun TypeLine(text: String, caret: Boolean = true) {
    Span({ classes("type"); style { property("--n", text.length.toString()) } }) { Text(text) }
    if (caret) Span({ classes("caret"); attr("aria-hidden", "true") })
}

enum class MeterTone(val cls: String?) { SIGNAL(null), STEEL("meter--steel"), MUTED("meter--muted"), INK("meter--ink") }

/** Segmented proficiency bar, exposed as an ARIA meter with the number also shown visually. */
@Composable
fun Meter(label: String, value: Int, tone: MeterTone = MeterTone.SIGNAL) {
    val v = value.coerceIn(0, 100)
    Div({ classes(*listOfNotNull("meter", tone.cls).toTypedArray()); markWhenSeen() }) {
        Span({ classes("meter__label") }) { Text(label) }
        Span({ classes("meter__val"); attr("aria-hidden", "true") }) { Text(v.toString().padStart(3, '0')) }
        Div({
            classes("meter__track")
            attr("role", "meter")
            attr("aria-label", label)
            attr("aria-valuemin", "0")
            attr("aria-valuemax", "100")
            attr("aria-valuenow", v.toString())
        }) {
            Div({ classes("meter__fill"); style { property("--v", v.toString()) } })
        }
    }
}

/** Status tag: glyph + colour + words, so status never relies on colour alone. */
@Composable
fun StatusTag(status: OpStatus) {
    val glyph = when (status) {
        OpStatus.DEPLOYED -> "▲"
        OpStatus.IN_PROGRESS -> "◐"
        OpStatus.RECON -> "◇"
    }
    Span({ classes("tag", "tag--${status.code}") }) {
        Span({ attr("aria-hidden", "true") }) { Text(glyph) }
        Span { Text(status.label) }
    }
}

@Composable
fun Chips(items: List<String>, label: String) {
    TagElement<HTMLElement>("ul", {
        classes("chips")
        attr("role", "list")
        attr("aria-label", label)
    }) {
        items.forEach { TagElement<HTMLElement>("li", { classes("chip") }) { Text(it) } }
    }
}

/** `<time>` isn't in Compose HTML's element set. */
@Composable
fun Time(datetime: String, content: ContentBuilder<HTMLElement>) {
    TagElement<HTMLElement>("time", { attr("datetime", datetime) }, content)
}

enum class BtnStyle(val cls: String?) { STEEL(null), SIGNAL("btn--signal"), SOLID("btn--solid") }

/** External link styled as a button. Same tab (no surprise windows), and no referrer/opener leakage. */
@Composable
fun ExtButton(href: String, label: String, style: BtnStyle = BtnStyle.STEEL, ariaLabel: String? = null) {
    A(href, {
        classes(*listOfNotNull("btn", style.cls).toTypedArray())
        attr("rel", "noopener noreferrer")
        ariaLabel?.let { attr("aria-label", it) }
    }) {
        Text(label)
        Span({ classes("btn__arrow"); attr("aria-hidden", "true") }) { Text("→") }
    }
}

@Composable
fun Todo(text: String) {
    Span({ classes("todo") }) { Text("TODO // $text") }
}

/** Convenience for `class` + `aria-hidden` decorative spans used across sections. */
@Composable
fun Deco(vararg cls: String, content: (@Composable ElementScope<HTMLElement>.() -> Unit)? = null) {
    Span({ classes(*cls); attr("aria-hidden", "true") }, content)
}

/**
 * Repo / live-demo buttons for an operation. Missing links show as CLASSIFIED rather than a dead button,
 * which keeps private or unfinished work on-theme instead of looking broken.
 */
@Composable
fun OpLinks(op: Operation) {
    if (op.repoUrl != null) {
        ExtButton(op.repoUrl, "Source", BtnStyle.SIGNAL, ariaLabel = "Source code: ${op.title}")
    } else {
        Span({ classes("readout", "classified") }) { Text("SOURCE // CLASSIFIED") }
    }
    if (op.liveUrl != null) {
        ExtButton(op.liveUrl, "Live demo", BtnStyle.STEEL, ariaLabel = "Live demo: ${op.title}")
    }
}

/*
 * Declassify on entry: a dossier card arrives with black bars over its key lines, which wipe away in sequence
 * as it scrolls into view, then a DECLASSIFIED stamp lands. The pre-rendered card is already declassified
 * (bars only exist on armed cards, see revealOnEntry), so nothing is ever hidden without JS or with reduced motion.
 */

/** Attrs for the card itself. */
fun AttrsScope<*>.declassify() {
    if (!Fx.DECLASSIFY) return
    classes("declass-card")
    revealOnEntry()
}

/** Attrs for a redacted line inside a [declassify] card. [order] staggers the wipe, top to bottom. */
fun AttrsScope<*>.declassLine(order: Int) {
    if (!Fx.DECLASSIFY) return
    classes("declass")
    style { property("--i", order.toString()) }
}

/** The stamp that lands once a [declassify] card's bars have cleared. */
@Composable
fun DeclassStamp() {
    if (!Fx.DECLASSIFY) return
    Span({ classes("declass-stamp"); attr("aria-hidden", "true"); parallax(0.05) }) {
        Stamp("Declassified", StampInk.AMBER, small = true)
    }
}
