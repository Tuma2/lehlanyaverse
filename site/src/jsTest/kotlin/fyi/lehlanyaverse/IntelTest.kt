package fyi.lehlanyaverse

import fyi.lehlanyaverse.data.Intel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Guards for Intel.kt: catch a bad entry before it ships. Run with `./gradlew :site:jsNodeTest`.
 */
class IntelTest {
    private val slugPattern = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
    private val phonePattern = Regex("""(\+?\d[\d\s().-]{7,}\d)""")

    @Test
    fun operationSlugsAreUniqueAndUrlSafe() {
        val slugs = Intel.operations.map { it.slug }
        assertEquals(slugs.size, slugs.toSet().size, "Duplicate operation slugs: $slugs")
        slugs.forEach { assertTrue(slugPattern.matches(it), "Slug '$it' must be lowercase letters, digits and dashes") }
    }

    @Test
    fun requiredTextIsPresent() {
        val op = Intel.operator
        listOf(op.name, op.callsign, op.role, op.location, op.status).forEach { assertTrue(it.isNotBlank()) }
        assertTrue(op.bio.isNotEmpty())
        Intel.operations.forEach { o ->
            assertTrue(o.title.isNotBlank() && o.summary.isNotBlank(), "Operation ${o.slug} needs a title and summary")
            assertTrue(o.objectives.isNotEmpty(), "Operation ${o.slug} needs at least one objective")
            assertTrue(o.stack.isNotEmpty(), "Operation ${o.slug} needs a stack")
        }
        Intel.serviceRecord.forEach { m -> assertTrue(m.objectives.isNotEmpty(), "${m.role} needs objectives") }
        Intel.fieldIntel.forEach { n -> assertTrue(n.title.isNotBlank() && n.note.isNotBlank()) }
    }

    @Test
    fun proficienciesAreInRange() {
        val l = Intel.loadout
        (l.primary + l.secondary + l.sidearms + l.equipment).forEach {
            assertTrue(it.proficiency in 0..100, "${it.name} proficiency ${it.proficiency} is outside 0–100")
        }
    }

    @Test
    fun externalLinksAreSecure() {
        val urls = Intel.operations.flatMap { listOfNotNull(it.repoUrl, it.liveUrl) } +
            Intel.fieldIntel.mapNotNull { it.url } +
            Intel.commendations.mapNotNull { it.credentialUrl } +
            Intel.comms.map { it.url }
        urls.forEach { url ->
            assertTrue(url.startsWith("https://") || url.startsWith("mailto:"), "Insecure or unexpected URL: $url")
        }
    }

    @Test
    fun operatorPhotosAreComplete() {
        val photos = Intel.operator.photos
        assertTrue(photos.isNotEmpty(), "The ID card needs at least one photo")
        photos.forEach {
            assertTrue(it.alt.isNotBlank(), "Photo ${it.src} needs alt text")
            assertTrue(it.caption.isNotBlank(), "Photo ${it.src} needs a caption")
            assertTrue(it.src.startsWith("/") && it.src.endsWith(".jpg"), "Photo ${it.src} should be a site-relative .jpg")
        }
    }

    @Test
    fun screenshotsHaveAltText() {
        Intel.operations.mapNotNull { it.screenshot }.forEach {
            assertTrue(it.alt.isNotBlank(), "Screenshot ${it.src} needs alt text")
            assertTrue(it.src.startsWith("/"), "Screenshot ${it.src} should be a site-relative path")
        }
    }

    @Test
    fun noPhoneNumberAnywhere() {
        // Coordinates and years are the only digit runs expected; none of them look like a phone number
        val everything = listOf(
            Intel.operator, Intel.loadout, Intel.serviceRecord, Intel.commendations,
            Intel.operations, Intel.fieldIntel, Intel.comms,
        ).joinToString(" ")
        phonePattern.find(everything)?.let { fail("Possible phone number in Intel: '${it.value}'") }
    }
}
