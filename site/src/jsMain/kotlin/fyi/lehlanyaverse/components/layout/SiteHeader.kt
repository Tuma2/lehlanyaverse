package fyi.lehlanyaverse.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import com.varabyte.kobweb.navigation.Anchor
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Header
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLDetailsElement
import org.w3c.dom.HTMLElement
import kotlin.js.Date

/** Dossier sections, in reading order. Anchors are absolute ("/#id") so they also work from detail pages. */
val dossierSections = listOf(
    "profile" to "Profile",
    "loadout" to "Loadout",
    "service-record" to "Service record",
    "commendations" to "Commendations",
    "operations" to "Operations",
    "field-intel" to "Field intel",
    "comms" to "Comms",
)

@Composable
fun SiteHeader() {
    Header({ classes("hud-header") }) {
        Div({ classes("wrap", "hud-header__inner") }) {
            Anchor("/", { classes("brand"); attr("aria-label", "LEHLANYAVERSE, home") }) {
                Text("LEHLANYAVERSE")
                Span({ classes("brand__slash"); attr("aria-hidden", "true") }) { Text(" //") }
            }
            MissionClock()
            Nav({ classes("nav"); attr("aria-label", "Dossier sections") }) {
                Ul({ classes("nav__list"); attr("role", "list") }) {
                    dossierSections.forEachIndexed { i, (id, label) -> Li { NavLink(i, id, label) } }
                }
                MobileMenu()
            }
        }
    }
}

@Composable
private fun NavLink(index: Int, id: String, label: String, onClick: (() -> Unit)? = null) {
    A("/#$id", {
        classes("nav__link")
        onClick?.let { cb -> onClick { cb() } }
    }) {
        Span({ attr("aria-hidden", "true") }) { Text("0${index + 1}") }
        Text(label)
    }
}

/** Native <details> disclosure: keyboard- and screen-reader-friendly, and works before JS has loaded. */
@Composable
private fun MobileMenu() {
    var details by remember { mutableStateOf<HTMLDetailsElement?>(null) }
    TagElement<HTMLDetailsElement>("details", {
        classes("nav-menu")
        ref { el -> details = el; onDispose { details = null } }
    }) {
        TagElement<HTMLElement>("summary", null) { Text("MENU") }
        Div({ classes("nav-menu__panel") }) {
            Ul({ attr("role", "list") }) {
                dossierSections.forEachIndexed { i, (id, label) ->
                    Li { NavLink(i, id, label) { details?.open = false } }
                }
            }
        }
    }
}

/** Live SAST clock. Decorative for screen readers (a per-second update would be noise). */
@Composable
private fun MissionClock() {
    var now by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        // Never bake a timestamp into the static export
        if (AppGlobals.isExporting) return@LaunchedEffect
        while (true) {
            now = sastNow()
            delay(1000)
        }
    }
    Div({ classes("clock"); attr("aria-hidden", "true") }) {
        Span({ classes("rec-dot") })
        Span({ classes("clock__label") }) { Text("MISSION CLOCK") }
        Span({ classes("clock__time") }) { Text(now ?: "--:--:--") }
        Span { Text("SAST") }
    }
}

/** South Africa has no daylight saving: SAST is always UTC+2. */
private fun sastNow(): String {
    val d = Date(Date.now() + 2 * 60 * 60 * 1000)
    fun two(n: Int) = n.toString().padStart(2, '0')
    return "${two(d.getUTCHours())}:${two(d.getUTCMinutes())}:${two(d.getUTCSeconds())}"
}
