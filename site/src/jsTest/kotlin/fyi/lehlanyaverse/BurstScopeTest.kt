package fyi.lehlanyaverse

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Keeps the maximalism contained: the burst accent (`--burst` / `--burst-glow`, violet) may only be used inside
 * hud.css section 8e (BURSTS), so it can never creep into normal content. Reads the stylesheet from disk (tests run
 * on Node).
 */
class BurstScopeTest {
    private val fs: dynamic = js("require('fs')")
    private val path: dynamic = js("require('path')")

    /** Walks up from the test's working directory to the repo, wherever Gradle runs Node from. */
    private fun hudCss(): String {
        var dir: String = js("process.cwd()")
        while (true) {
            val candidate: String = path.join(dir, "site", "src", "jsMain", "resources", "public", "css", "hud.css")
            if (fs.existsSync(candidate) as Boolean) return fs.readFileSync(candidate, "utf8") as String
            val parent: String = path.dirname(dir)
            if (parent == dir) fail("hud.css not found above ${js("process.cwd()")}")
            dir = parent
        }
    }

    @Test
    fun burstAccentOnlyInBurstSection() {
        val lines = hudCss().lines()
        val start = lines.indexOfFirst { it.startsWith("/* 8e. BURSTS") }
        val end = lines.indexOfFirst { it.startsWith("/* 9. MOTION") }
        assertTrue(start in 0 until end, "hud.css must keep section 8e (BURSTS) before section 9")
        val accent = Regex("""var\(--burst(-glow)?\)""")
        val leaks = lines.withIndex().filter { (i, l) -> accent.containsMatchIn(l) && i !in start until end }
        assertTrue(leaks.isEmpty(), "--burst used outside section 8e, at hud.css lines ${leaks.map { it.index + 1 }}")
    }
}
