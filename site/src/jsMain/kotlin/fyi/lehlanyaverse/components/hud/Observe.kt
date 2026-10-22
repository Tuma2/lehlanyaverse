package fyi.lehlanyaverse.components.hud

import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.Element

/** Minimal binding: IntersectionObserver isn't part of Kotlin/JS's bundled DOM API. */
private external class IntersectionObserver(
    callback: (entries: Array<dynamic>, observer: IntersectionObserver) -> Unit,
    options: dynamic = definedExternally,
) {
    fun observe(target: Element)
    fun disconnect()
}

/**
 * Adds `is-seen` the first time the element scrolls into view, which CSS uses to play a one-shot entrance.
 * Skipped during static export so the snapshot always holds the final, fully rendered state.
 */
fun AttrsScope<*>.markWhenSeen() {
    ref { el ->
        if (AppGlobals.isExporting) return@ref onDispose { }
        val io = IntersectionObserver({ entries, obs ->
            if (entries.any { it.isIntersecting as Boolean }) {
                el.classList.add("is-seen")
                obs.disconnect()
            }
        }, js("({ threshold: 0.35 })"))
        io.observe(el)
        onDispose { io.disconnect() }
    }
}
