package fyi.lehlanyaverse

import com.varabyte.kobweb.core.init.InitKobweb
import com.varabyte.kobweb.core.init.InitKobwebContext
import fyi.lehlanyaverse.components.layout.SITE_NAME
import fyi.lehlanyaverse.components.layout.Seo
import fyi.lehlanyaverse.components.sections.SignalLost

/**
 * Unknown routes: GitHub Pages serves 404.html, then Kobweb's client router re-resolves the URL.
 * Without this, the router swaps our themed page for its plain default error screen.
 */
@InitKobweb
fun initErrorPage(ctx: InitKobwebContext) {
    ctx.router.setErrorPage(layoutId = "fyi.lehlanyaverse.components.layout.PageLayout") {
        Seo(title = "Signal lost // $SITE_NAME", description = "This frequency is dead. Return to the dossier.", path = "/404", noindex = true)
        SignalLost("No transmission on this frequency. The page may have moved, or the coordinates were wrong.")
    }
}
