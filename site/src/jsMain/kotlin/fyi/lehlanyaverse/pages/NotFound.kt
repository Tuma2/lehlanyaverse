package fyi.lehlanyaverse.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import fyi.lehlanyaverse.components.layout.SITE_NAME
import fyi.lehlanyaverse.components.layout.Seo
import fyi.lehlanyaverse.components.sections.SignalLost

/** Exported as /404.html, which GitHub Pages serves for unknown paths. */
@Page("404")
@Composable
fun NotFoundPage() {
    Seo(title = "Signal lost // $SITE_NAME", description = "This frequency is dead. Return to the dossier.", path = "/404", noindex = true)
    SignalLost("No transmission on this frequency. The page may have moved, or the coordinates were wrong.")
}
