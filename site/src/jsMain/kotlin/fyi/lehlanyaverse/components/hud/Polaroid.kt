package fyi.lehlanyaverse.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fyi.lehlanyaverse.data.Photo
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

/*
 * Polaroid photos: a single print, a pinned print, and a clipped stack that cycles on tap.
 * All look and motion lives in hud.css ("Polaroids"); these only set state and data attributes.
 */

enum class PinStyle(val cls: String) { RED("pinned--red"), ACID("pinned--acid") }

enum class ClipStyle(val cls: String?) { STEEL("pstack--clip"), NONE(null) }

/** Matches the swap keyframes in hud.css; taps are ignored until it has elapsed. */
private const val SWAP_MS = 420

/**
 * One Polaroid print. [exposeAlt] = false hides it from assistive tech entirely (a print buried in a stack).
 * A .jpg [Photo.src] gets its generated .webp sibling as the preferred source.
 */
@Composable
fun Polaroid(photo: Photo, exposeAlt: Boolean = true, attrs: (AttrsScope<HTMLElement>.() -> Unit)? = null) {
    TagElement<HTMLElement>("figure", {
        classes("polaroid")
        if (!exposeAlt) attr("aria-hidden", "true")
        attrs?.invoke(this)
    }) {
        Span({ classes("polaroid__img") }) {
            TagElement<HTMLElement>("picture", null) {
                if (photo.src.endsWith(".jpg")) {
                    TagElement<HTMLElement>("source", {
                        attr("type", "image/webp")
                        attr("srcset", photo.src.removeSuffix(".jpg") + ".webp")
                    }, null)
                }
                Img(photo.src, if (exposeAlt) photo.alt else "", {
                    attr("width", "600")
                    attr("height", "600")
                    attr("decoding", "async")
                })
            }
        }
        TagElement<HTMLElement>("figcaption", { classes("polaroid__caption") }) {
            Span { Text(photo.caption) }
            photo.note?.let { Span({ classes("polaroid__note") }) { Text(it) } }
        }
    }
}

/** A single print pushed into the page with a pushpin; straightens and regains colour on hover. */
@Composable
fun PinnedPolaroid(photo: Photo, rotation: Int = -4, pin: PinStyle = PinStyle.RED) {
    Div({ classes("pinned", pin.cls); style { property("--rot", "${rotation}deg") } }) {
        Polaroid(photo)
        Deco("pinned__pin")
    }
}

/**
 * A paper-clipped stack of prints. Tapping the top print sends it to the back and brings up the next, cycling in
 * order. [rotations] are the resting angles for the top, second and every deeper position.
 *
 * The photos and the button are siblings (a <figure> can't live inside a <button>, and the button's label would
 * mask the alt text), so the button overlays the top print and only that print is exposed to assistive tech.
 */
@Composable
fun PolaroidStack(
    photos: List<Photo>,
    label: String = "photo",
    rotations: List<Int> = listOf(-4, 5, -1),
    clip: ClipStyle = ClipStyle.STEEL,
    hint: String? = "TAP TO FLIP",
) {
    val n = photos.size
    var top by remember { mutableStateOf(0) }
    var leaving by remember { mutableStateOf<Int?>(null) }
    var announcement by remember { mutableStateOf("") }
    var button by remember { mutableStateOf<HTMLElement?>(null) }

    fun next() {
        if (n < 2 || leaving != null) return
        leaving = top
        top = (top + 1) % n
        announcement = "Photo ${top + 1} of $n: ${photos[top].alt}"
        rememberFlipped()
        window.setTimeout({ leaving = null }, SWAP_MS)
    }

    // A tap before the app started was caught by boot.js; replay it now that the stack is interactive
    LaunchedEffect(Unit) {
        val pending = window.asDynamic().lvPendingFlip
        if (pending != null && pending != undefined) {
            window.asDynamic().lvPendingFlip = null
            next()
            if (pending == "key") button?.focus()
        }
    }

    Div({
        classes(*listOfNotNull("pstack", clip.cls).toTypedArray())
        rotations.take(3).forEachIndexed { i, deg -> style { property("--rot$i", "${deg}deg") } }
    }) {
        Div({ classes("pstack__photos") }) {
            photos.forEachIndexed { i, photo ->
                val pos = (i - top + n) % n
                Polaroid(photo, exposeAlt = pos == 0) {
                    classes("pstack__item")
                    if (i == leaving) classes("is-leaving")
                    attr("data-pos", minOf(pos, 2).toString())
                    style { property("z-index", (n - pos).toString()) }
                }
            }
            if (n > 1) {
                Button({
                    classes("pstack__btn")
                    attr("type", "button")
                    attr("data-lock", "FLIP PHOTO") // label for the cursor reticle (fx/Reticle.kt)
                    attr("aria-label", "Show next $label (${top + 1} of $n)")
                    ref { el -> button = el; onDispose { button = null } }
                    onClick { next() }
                })
            }
        }
        if (clip != ClipStyle.NONE) Deco("pstack__clip")
        if (n > 1 && hint != null) Deco("pstack__hint") { Text(hint) }
        Span({ classes("sr-only"); attr("aria-live", "polite") }) { Text(announcement) }
    }
}

/** The "tap to flip" hint is shown until the visitor's first flip (boot.js reads this before first paint). */
private fun rememberFlipped() {
    document.documentElement?.setAttribute("data-flipped", "")
    try {
        window.localStorage.setItem("lv.flipped", "1")
    } catch (e: Throwable) {
        // Storage blocked: the hint simply comes back on the next visit
    }
}
