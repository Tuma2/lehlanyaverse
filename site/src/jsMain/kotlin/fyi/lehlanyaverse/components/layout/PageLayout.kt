package fyi.lehlanyaverse.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import com.varabyte.kobweb.core.layout.Layout
import fyi.lehlanyaverse.components.fx.DeployFlash
import fyi.lehlanyaverse.components.fx.Reticle
import fyi.lehlanyaverse.components.fx.SmoothScroll
import fyi.lehlanyaverse.components.fx.parallaxLayer
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Text

/** Shared scaffolding for every page: skip link, FX layers, header, ticker, main landmark, footer. */
@Layout
@Composable
fun PageLayout(content: @Composable () -> Unit) {
    // Lets CSS start JS-dependent motion only after Kobweb has mounted over the pre-rendered HTML
    // (never during export, or the class would be baked into the static HTML)
    LaunchedEffect(Unit) {
        if (!AppGlobals.isExporting) document.documentElement?.classList?.add("js-ready")
        SmoothScroll.init()
    }

    A("#main", { classes("skip-link") }) { Text("Skip to dossier") }
    FxLayers()
    SiteHeader()
    Ticker()
    Main({ id("main"); tabIndex(-1) }) {
        Div({ classes("wrap") }) { content() }
    }
    SiteFooter()
    DeployFlash.Overlay()
    Reticle()
}

@Composable
private fun FxLayers() {
    // Map and grid sit at different depths beneath the dossier, so they drift by different amounts
    val drift = mapOf("fx--grid" to 20.0, "fx--topo" to 36.0)
    listOf("fx--grid", "fx--topo", "fx--vignette", "fx--scan", "fx--grain").forEach { layer ->
        Div({
            classes("fx", layer)
            attr("aria-hidden", "true")
            drift[layer]?.let { parallaxLayer(it) }
        })
    }
}
