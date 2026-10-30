package fyi.lehlanyaverse.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import fyi.lehlanyaverse.components.layout.SITE_NAME
import fyi.lehlanyaverse.components.layout.Seo
import fyi.lehlanyaverse.components.sections.OffAir

/**
 * Exported as /maintenance.html. The Deploy workflow's `maintenance` switch publishes it as index.html and 404.html
 * (app script stripped), keeping /cv/ in place, so the CV stays downloadable while the site is offline.
 * PageLayout drops the header, ticker and footer on this route: their links point at sections that aren't there.
 */
@Page("maintenance")
@Composable
fun MaintenancePage() {
    Seo(title = "Off air // $SITE_NAME", description = "The dossier is down for maintenance. The CV is still on file.", path = "/maintenance", noindex = true)
    OffAir()
}
