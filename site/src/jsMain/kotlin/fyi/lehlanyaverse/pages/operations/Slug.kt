package fyi.lehlanyaverse.pages.operations

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import fyi.lehlanyaverse.components.layout.SITE_NAME
import fyi.lehlanyaverse.components.layout.Seo
import fyi.lehlanyaverse.components.sections.OperationDebrief
import fyi.lehlanyaverse.components.sections.SignalLost
import fyi.lehlanyaverse.data.Intel

/** /operations/{slug}: one page per Intel.operations entry, pre-rendered at export (see site/build.gradle.kts). */
@Page("{slug}")
@Composable
fun OperationPage() {
    val slug = rememberPageContext().route.params["slug"]
    val index = Intel.operations.indexOfFirst { it.slug == slug }
    if (index < 0) {
        Seo(title = "Operation not found // $SITE_NAME", description = "No operation on file with that codename.", path = "/operations/$slug", noindex = true)
        SignalLost("No operation on file under \"$slug\".")
        return
    }
    val op = Intel.operations[index]
    Seo(
        title = "${op.title}: ${op.codename} // $SITE_NAME",
        description = op.summary,
        path = "/operations/${op.slug}",
        ogImage = "/og/${op.slug}.png",
    )
    OperationDebrief(index)
}
