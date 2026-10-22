package fyi.lehlanyaverse.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import fyi.lehlanyaverse.data.Intel
import kotlinx.browser.document
import org.w3c.dom.Element

const val SITE_URL = "https://lehlanyaverse.fyi"
const val SITE_NAME = "LEHLANYAVERSE"

/**
 * Per-page <head> metadata. Kobweb's static export snapshots the DOM after render,
 * so whatever this writes is baked into each exported HTML file for crawlers and link previews.
 */
@Composable
fun Seo(
    title: String,
    description: String,
    path: String,
    ogImage: String = "/og/og-default.png",
    noindex: Boolean = false,
) {
    SideEffect {
        val url = SITE_URL + path
        val image = SITE_URL + ogImage
        document.title = title
        meta("name", "description", description)
        link("canonical", url)
        meta("property", "og:type", "website")
        meta("property", "og:site_name", SITE_NAME)
        meta("property", "og:title", title)
        meta("property", "og:description", description)
        meta("property", "og:url", url)
        meta("property", "og:image", image)
        meta("property", "og:image:width", "1200")
        meta("property", "og:image:height", "630")
        meta("property", "og:image:alt", "$SITE_NAME mission dossier card")
        meta("name", "twitter:card", "summary_large_image")
        meta("name", "twitter:title", title)
        meta("name", "twitter:description", description)
        meta("name", "twitter:image", image)
        meta("name", "robots", if (noindex) "noindex" else "index, follow")
    }
}

private fun upsert(selector: String, create: () -> Element): Element =
    document.head!!.querySelector(selector) ?: create().also { document.head!!.appendChild(it) }

private fun meta(keyAttr: String, key: String, content: String) {
    upsert("meta[$keyAttr=\"$key\"]") {
        document.createElement("meta").apply { setAttribute(keyAttr, key) }
    }.setAttribute("content", content)
}

private fun link(rel: String, href: String) {
    upsert("link[rel=\"$rel\"]") {
        document.createElement("link").apply { setAttribute("rel", rel) }
    }.setAttribute("href", href)
}

/** schema.org Person for the home page. A non-executable data block, so the CSP's script-src doesn't apply. */
@Composable
fun PersonJsonLd() {
    SideEffect {
        val op = Intel.operator
        val sameAs = Intel.comms.filter { it.url.startsWith("https://") }.joinToString(",") { "\"${it.url}\"" }
        val json = """{"@context":"https://schema.org","@type":"Person","name":"${op.name}","alternateName":"${op.callsign}",""" +
            """"jobTitle":"${op.role}","url":"$SITE_URL","address":{"@type":"PostalAddress","addressLocality":"Johannesburg","addressCountry":"ZA"},""" +
            """"knowsAbout":[${Intel.loadout.primary.joinToString(",") { "\"${it.name}\"" }}],"sameAs":[$sameAs]}"""
        upsert("script[type=\"application/ld+json\"]") {
            document.createElement("script").apply { setAttribute("type", "application/ld+json") }
        }.textContent = json
    }
}
