package fyi.lehlanyaverse.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import com.varabyte.kobweb.core.layout.Layout
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
    }

    A("#main", { classes("skip-link") }) { Text("Skip to dossier") }
    FxLayers()
    SiteHeader()
    Ticker()
    Main({ id("main"); tabIndex(-1) }) {
        Div({ classes("wrap") }) { content() }
    }
    SiteFooter()
}

@Composable
private fun FxLayers() {
    listOf("fx--grid", "fx--topo", "fx--vignette", "fx--scan", "fx--grain").forEach { layer ->
        Div({ classes("fx", layer); attr("aria-hidden", "true") })
    }
}
