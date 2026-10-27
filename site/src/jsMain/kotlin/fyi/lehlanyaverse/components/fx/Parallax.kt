package fyi.lehlanyaverse.components.fx

import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement
import kotlin.math.abs

/** Hard ceiling for any drift, so depth reads as texture and never as things sliding around. */
private const val MAX_DRIFT_PX = 16.0

/** Narrow windows get half the drift: less room, and the content is closer to the eye. (Touch devices get none.) */
private const val PHONE_MAX_W = 768.0

/*
 * Parallax depth for the table-top feel. Every offset is written to the individual `translate` property, which
 * composes with the `rotate` and `transform` the elements already use (stamps, the photo stack) instead of
 * replacing them. All layout reads happen in measure passes; frames only do arithmetic and write `translate`.
 */

/**
 * Fixed background layer (grid, topographic map): slides up by at most [maxPx] over the whole page, so it moves
 * slower than the content and reads as further away. The layer is oversized in CSS to cover the shift.
 */
fun AttrsScope<*>.parallaxLayer(maxPx: Double) {
    ref { node ->
        val el = node.unsafeCast<HTMLElement>()
        if (!Fx.PARALLAX || !Fx.motion || Fx.touchOnly) return@ref onDispose { }
        var applied = Double.NaN
        val dispose = SmoothScroll.onFrame { f ->
            val y = (-f.progress * maxPx * 10).toInt() / 10.0
            if (y != applied) {
                el.style.setProperty("translate", "0 ${y}px")
                applied = y
            }
        }
        onDispose { dispose(); el.style.removeProperty("translate") }
    }
}

/**
 * In-flow element sitting on top of the dossier (a photo, a stamp): drifts by [factor] × its distance from the
 * viewport centre, capped at [MAX_DRIFT_PX]. A positive factor moves it slightly faster than the page beneath,
 * as if it were lying a little closer to the camera.
 */
fun AttrsScope<*>.parallax(factor: Double) {
    ref { node ->
        val el = node.unsafeCast<HTMLElement>()
        if (!Fx.PARALLAX || !Fx.motion || Fx.touchOnly) return@ref onDispose { }
        // Own compositor layer: moving a filtered photo would otherwise repaint it every frame
        el.classList.add("fx-drift")
        val layer = Drift(el, factor)
        val unMeasure = SmoothScroll.onMeasure { layer.measure() }
        val unFrame = SmoothScroll.onFrame { layer.frame(it) }
        onDispose { unFrame(); unMeasure(); el.classList.remove("fx-drift"); el.style.removeProperty("translate") }
    }
}

private class Drift(private val el: HTMLElement, private val factor: Double) {
    /** Scroll position at which the element sits exactly where the layout put it (zero drift). */
    private var restY = 0.0
    private var applied = 0.0

    fun measure() {
        val r = el.getBoundingClientRect()
        // Page position, minus the drift currently applied
        val top = r.top + window.scrollY - applied
        val centre = top + r.height / 2
        // Normally at rest when centred in the viewport. Anything visible at the very top of the page rests at
        // scroll 0 instead, so the hero never shifts when the app starts.
        restY = if (top < window.innerHeight) 0.0 else centre - window.innerHeight / 2.0
    }

    fun frame(f: ScrollFrame) {
        val fromRest = restY - f.y
        // Far off screen: nothing to see, nothing to write
        if (abs(fromRest) > f.viewportH * 1.5) return
        val amp = if (f.viewportW < PHONE_MAX_W) 0.5 else 1.0
        val y = ((fromRest * factor * amp).coerceIn(-MAX_DRIFT_PX, MAX_DRIFT_PX) * 10).toInt() / 10.0
        if (y != applied) {
            el.style.setProperty("translate", "0 ${y}px")
            applied = y
        }
    }
}
