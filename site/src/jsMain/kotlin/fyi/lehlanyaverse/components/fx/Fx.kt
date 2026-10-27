package fyi.lehlanyaverse.components.fx

import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import kotlinx.browser.window

/**
 * Scroll effects, one switch each. A flag set to false removes the effect entirely: its markup isn't rendered
 * and no listener is attached. Content never depends on any of them.
 */
object Fx {
    /** Lenis smooth wheel scrolling (touch always stays native). */
    const val SMOOTH_SCROLL = true

    /** Header bar + "MISSION PROGRESS 042%" readout. */
    const val PROGRESS = true

    /** Waypoint rail on the right edge of wide screens (home page only). */
    const val TRACKER = true

    /** "DEPLOYING TO SECTOR 03" flash before an in-page jump. */
    const val DEPLOY_FLASH = true

    /** Redaction bars wipe off dossier cards as they enter view, then a stamp lands. */
    const val DECLASSIFY = true

    /** "SECTOR 0X // NAME" projected above each section heading; flickers on as the section enters view. */
    const val PROJECTED_HEADERS = true

    /** "VIS" light meter in the header strip: rises with scroll speed, sinks back to HIDDEN when still. */
    const val VISIBILITY = true

    /** Burst moments step into a spotlight as they scroll into view and fall back into shadow as they leave. */
    const val BURST_LIGHT = true

    /** Fake lat/long + grid ref in the header that pans with scroll. */
    const val COORDINATES = true

    /** Background layers, photos and stamps drift at different depths. */
    const val PARALLAX = true

    /** Target-lock brackets around the crosshair cursor (mouse/trackpad only). The CSS crosshair stays either way. */
    const val CURSOR_FX = true

    /** False during the export snapshot and for visitors who prefer reduced motion: gates every motion effect. */
    val motion: Boolean
        get() = !AppGlobals.isExporting && !window.matchMedia("(prefers-reduced-motion: reduce)").matches

    /**
     * Touch-only device (phone, tablet). These scroll natively, and the URL bar resizes the viewport as it slides,
     * so scroll-linked effects that move or relight things (smooth scroll, parallax, burst shading) stay off there.
     */
    val touchOnly: Boolean
        get() = window.matchMedia("(hover: none) and (pointer: coarse)").matches
}
