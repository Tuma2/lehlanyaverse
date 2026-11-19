package fyi.lehlanyaverse

import fyi.lehlanyaverse.data.AssessmentKind
import fyi.lehlanyaverse.data.CV_SIZE_BYTES
import fyi.lehlanyaverse.data.CommendationGroup
import fyi.lehlanyaverse.data.CommendationKind
import fyi.lehlanyaverse.data.Intel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        listOf(op.name, op.callsign, op.role, op.location, op.status, op.availabilityNote).forEach { assertTrue(it.isNotBlank()) }
        assertTrue(op.bio.isNotEmpty())
        Intel.operations.forEach { o ->
            assertTrue(o.title.isNotBlank() && o.summary.isNotBlank(), "Operation ${o.slug} needs a title and summary")
            assertTrue(o.objectives.isNotEmpty(), "Operation ${o.slug} needs at least one objective")
            assertTrue(o.stack.isNotEmpty(), "Operation ${o.slug} needs a stack")
        }
        Intel.serviceRecord.forEach { m -> assertTrue(m.objectives.isNotEmpty(), "${m.role} needs objectives") }
        Intel.fieldIntel.forEach { n ->
            assertTrue(n.title.isNotBlank() && n.teaser.isNotBlank() && n.summary.isNotBlank(), "'${n.id}' needs a title, teaser and summary")
        }
    }

    @Test
    fun fieldIntelLinksResolve() {
        val ids = Intel.fieldIntel.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Field intel ids must be unique: $ids")
        Intel.fieldIntel.forEach { n ->
            assertTrue(n.id.isNotBlank() && " " !in n.id, "Field intel id '${n.id}' must be a slug")
            n.linkedTo.forEach { assertTrue(it in ids && it != n.id, "'${n.id}' links to unknown or self id '$it'") }
        }
    }

    @Test
    fun fieldIntelPapersStayOnTheBoard() {
        Intel.fieldIntel.forEach { n ->
            n.position?.let { assertTrue(it.x in 0.0..100.0 && it.y in 0.0..100.0, "'${n.id}' position $it is off the board (0–100%)") }
            n.rotation?.let { assertTrue(it in -15.0..15.0, "'${n.id}' rotation $it° is more than ±15°") }
        }
    }

    @Test
    fun proficienciesAreInRange() {
        val l = Intel.loadout
        (l.primary + l.integration + l.testing + l.secondary + l.sidearms + l.equipment).forEach {
            assertTrue(it.proficiency in 0..100, "${it.name} proficiency ${it.proficiency} is outside 0–100")
        }
    }

    @Test
    fun onlyTheCvMediumSkillsSitBelowHigh() {
        // Per the CV, only pytest, Kotlin and Python are medium; everything else in these slots is high (70+)
        val medium = setOf("pytest", "Kotlin", "Python (FastAPI)")
        val l = Intel.loadout
        (l.primary + l.integration + l.testing + l.secondary).forEach {
            if (it.name in medium) assertTrue(it.proficiency in 40..69, "${it.name} should be medium, is ${it.proficiency}")
            else assertTrue(it.proficiency >= 70, "${it.name} should be high (70+), is ${it.proficiency}")
        }
    }

    @Test
    fun externalLinksAreSecure() {
        val urls = Intel.operations.flatMap { listOfNotNull(it.repoUrl, it.liveUrl) } +
            Intel.fieldIntel.mapNotNull { it.link } +
            Intel.commendations.mapNotNull { it.credentialUrl } +
            Intel.skillAssessments.mapNotNull { it.verifyUrl } +
            listOfNotNull(Intel.hackerRankProfileUrl) +
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
    fun skillAssessmentsAreWellFormed() {
        val l = Intel.loadout
        val loadoutNames = (l.primary + l.integration + l.testing + l.secondary + l.sidearms + l.equipment).map { it.name }.toSet()
        val names = Intel.skillAssessments.map { it.name }
        assertEquals(names.size, names.toSet().size, "Duplicate skill assessments: $names")
        Intel.skillAssessments.forEach { a ->
            assertTrue(a.relatedSkill in loadoutNames, "'${a.name}' backs '${a.relatedSkill}', which isn't a LOADOUT skill: $loadoutNames")
            when (a.kind) {
                AssessmentKind.BADGE -> assertTrue(a.stars in 1..5, "'${a.name}' badge needs 1–5 stars, has ${a.stars}")
                AssessmentKind.CERTIFICATE -> assertEquals(null, a.stars, "'${a.name}' is a certificate: stars are for badges")
            }
            a.verifyUrl?.let { assertTrue(Regex("""^https://(www\.)?hackerrank\.com/""").containsMatchIn(it), "'${a.name}' verify URL should be a hackerrank.com link: $it") }
        }
        Intel.hackerRankProfileUrl?.let {
            assertTrue(Regex("""^https://(www\.)?hackerrank\.com/""").containsMatchIn(it), "HackerRank profile URL should be a hackerrank.com link: $it")
        }
    }

    @Test
    fun everyCommendationGroupIsFilled() {
        val used = Intel.commendations.map { it.group }.toSet()
        CommendationGroup.entries.forEach { assertTrue(it in used, "Commendation group '${it.label}' is empty") }
    }

    @Test
    fun certificatesAreNotCalledCertifications() {
        // A completion or training certificate is not an accredited certification: its title must not claim one
        Intel.commendations
            .filter { it.kind == CommendationKind.COMPLETION || it.kind == CommendationKind.TRAINING }
            .forEach { assertFalse(Regex("certified|certification", RegexOption.IGNORE_CASE).containsMatchIn(it.title), "'${it.title}' is a ${it.kind.label}, not a certification") }
    }

    @Test
    fun securityCertsHangInTheClearanceCluster() {
        // The medal wall frames isClearance groups as "Security clearance": ISC2 CC and both ISO 27001 entries belong there
        val clearance = Intel.commendations.filter { it.group.isClearance }.map { it.title }
        assertEquals(2, clearance.count { "27001" in it }, "Both ISO/IEC 27001 entries must be in the clearance cluster: $clearance")
        assertTrue(clearance.any { "Certified in Cybersecurity" in it }, "ISC2 CC must be in the clearance cluster: $clearance")
    }

    @Test
    fun noPhoneNumberAnywhere() {
        // Coordinates, years and ISO dates are the only digit runs expected; none of them look like a phone number
        val everything = listOf(
            Intel.operator, Intel.loadout, Intel.serviceRecord, Intel.commendations,
            Intel.operations, Intel.fieldIntel, Intel.comms, Intel.cv,
        ).joinToString(" ").replace(Regex("""\b\d{4}-\d{2}-\d{2}\b"""), "DATE") // ISO dates (cv.updated) aren't phones
        phonePattern.find(everything)?.let { fail("Possible phone number in Intel: '${it.value}'") }
    }

    @Test
    fun cvMatchesItsFile() {
        val cv = Intel.cv
        assertTrue(cv.path.startsWith("/cv/") && cv.path.endsWith(".pdf"), "CV path '${cv.path}' must be /cv/<name>.pdf")
        assertEquals(cv.path.substringAfterLast('/'), cv.fileName, "CV download name must match the file name")
        assertTrue(
            Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$""").matches(cv.updated),
            "CV updated '${cv.updated}' must be an ISO date, yyyy-mm-dd",
        )
        assertEquals("/cv.pdf?v=${cv.updated}", cv.href, "CV links must use the stable /cv.pdf with the version query")
        assertTrue(Regex("""^PDF · \d+(\.\d)? (KB|MB)$""").matches(cv.size), "CV size '${cv.size}' must read like 'PDF · 90 KB'")

        // The file itself: only on Node (CI's jsNodeTest), where the repo is on disk
        if (js("typeof require === 'undefined'") as Boolean) return
        val fs: dynamic = js("require('fs')")
        val path: dynamic = js("require('path')")
        var dir: String = js("process.cwd()")
        while (true) {
            val file: String = path.join(dir, "site", "src", "jsMain", "resources", "public", cv.path.removePrefix("/"))
            if (fs.existsSync(file) as Boolean) {
                val bytes = (fs.statSync(file).size as Number).toDouble()
                assertTrue(bytes < 2.0 * 1024 * 1024, "CV is ${(bytes / 1024).toInt()} KB: keep it under 2 MB")
                val magic: String = fs.readFileSync(file).subarray(0, 5).toString("latin1")
                assertEquals("%PDF-", magic, "CV file ${cv.path} is not a PDF")
                assertEquals(CV_SIZE_BYTES.toDouble(), bytes, "Generated CV size label is stale: rebuild (generateCvMeta)")
                return
            }
            val parent: String = path.dirname(dir)
            if (parent == dir) fail("CV file ${cv.path} not found under site/src/jsMain/resources/public")
            dir = parent
        }
    }
}
