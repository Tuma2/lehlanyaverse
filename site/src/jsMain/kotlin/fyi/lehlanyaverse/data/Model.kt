package fyi.lehlanyaverse.data

/*
 * Content model for the dossier. Layout code only ever reads these types;
 * the actual values live in Intel.kt.
 */

data class Operator(
    val name: String,
    /** Rendered as a redaction bar that declassifies on hover / focus. */
    val callsign: String,
    val role: String,
    val specialty: String,
    val location: String,
    val coordinates: String,
    val status: String,
    val bio: List<String>,
    val clearance: String,
    /** Headline numbers for the profile card, e.g. Stat("4+", "Years in the field"). */
    val stats: List<Stat>,
)

data class Stat(val value: String, val label: String)

data class Skill(
    val name: String,
    /** 0–100. Rendered as a proficiency bar with the number shown alongside. */
    val proficiency: Int,
)

data class Loadout(
    val primary: List<Skill>,
    val secondary: List<Skill>,
    val sidearms: List<Skill>,
    val equipment: List<Skill>,
)

data class Mission(
    val codename: String,
    val role: String,
    val unit: String,
    /** Display text, e.g. "JUL 2025". */
    val start: String,
    /** Machine-readable ISO date for <time datetime>, e.g. "2025-07". */
    val startIso: String,
    /** Null means ongoing. */
    val end: String?,
    val endIso: String?,
    val briefing: String,
    val objectives: List<String>,
    val stack: List<String>,
)

enum class CommendationKind(val label: String) {
    CERTIFICATION("CERTIFICATION"),
    EDUCATION("EDUCATION"),
}

data class Commendation(
    val title: String,
    val issuer: String,
    val kind: CommendationKind,
    val awarded: Int,
    val expires: Int? = null,
    val credentialUrl: String? = null,
)

enum class OpStatus(val label: String, val code: String) {
    IN_PROGRESS("IN PROGRESS", "inprogress"),
    DEPLOYED("DEPLOYED", "deployed"),
    RECON("RECON", "recon"),
}

/** Alt text is mandatory: a screenshot cannot be added without describing it. */
data class Screenshot(val src: String, val alt: String)

data class Operation(
    /** URL segment for /operations/{slug}: lowercase letters, digits and dashes. */
    val slug: String,
    val codename: String,
    val title: String,
    val status: OpStatus,
    val summary: String,
    val briefing: List<String>,
    val objectives: List<String>,
    val stack: List<String>,
    val repoUrl: String? = null,
    val liveUrl: String? = null,
    val screenshot: Screenshot? = null,
)

data class FieldNote(
    val title: String,
    val note: String,
    val tags: List<String>,
    /** ISO date, e.g. "2026-10". */
    val logged: String,
    val url: String? = null,
)

enum class CommsChannel { EMAIL, LINKEDIN, GITHUB }

data class CommsLink(
    val channel: CommsChannel,
    val label: String,
    val handle: String,
    val url: String,
)
