import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.unsafe
import java.security.MessageDigest
import java.util.Base64
import java.util.Locale

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
}

group = "fyi.lehlanyaverse"
version = "1.0.0"

val siteUrl = "https://lehlanyaverse.fyi"
val intelFile = layout.projectDirectory.file("src/jsMain/kotlin/fyi/lehlanyaverse/data/Intel.kt")

/**
 * Operation slugs, read straight out of Intel.kt so adding a project there is the only step needed:
 * every slug becomes a pre-rendered /operations/{slug} page and a sitemap entry.
 */
val operationSlugs: List<String> = Regex("""slug\s*=\s*"([a-z0-9-]+)"""")
    .findAll(intelFile.asFile.readText())
    .map { it.groupValues[1] }
    .toList()

/**
 * Boot-intro gate, inlined into <head> so it runs before first paint without a render-blocking request.
 * The CSP allows exactly this script by its SHA-256 hash, so script-src stays free of 'unsafe-inline'.
 */
val bootScript: String = layout.projectDirectory.file("src/head/boot.js").asFile.readText().trim()
val bootScriptHash: String = "'sha256-" + Base64.getEncoder().encodeToString(
    MessageDigest.getInstance("SHA-256").digest(bootScript.toByteArray(Charsets.UTF_8))
) + "'"

/** Static export only pre-renders a dynamic route for the concrete paths it is told about. */
val exportRoutes: List<String> = listOf("/") + operationSlugs.map { "/operations/$it" }

kobweb {
    app {
        index {
            lang.set("en")
            description.set("Sebabatso Moleele: Java backend software engineer in Johannesburg. Mission dossier, loadout and active operations.")
            faviconPath.set("/favicon.svg")
            // Pages are pre-rendered, so the app bundle never needs to block first paint: Kobweb emits it as an
            // inert script and src/head/boot.js starts it on the frame after first paint
            scriptAttributes.put("type", "text/x-kobweb-app")
            // Copied into locals so the head lambda captures plain strings, not the build script (config cache)
            val boot = bootScript
            val bootHash = bootScriptHash
            head.add {
                // CSP as a meta tag (GitHub Pages cannot set response headers). Defined inside this lambda so the
                // configuration cache does not capture a script reference.
                // 'unsafe-inline' styles are required: Compose HTML writes inline style attributes.
                val csp = listOf(
                    "default-src 'self'",
                    "script-src 'self' $bootHash",
                    "style-src 'self' 'unsafe-inline'",
                    "img-src 'self' data:",
                    "font-src 'self'",
                    "connect-src 'self'",
                    "manifest-src 'self'",
                    "base-uri 'self'",
                    "form-action 'none'",
                    "object-src 'none'",
                ).joinToString("; ")
                meta { httpEquiv = "Content-Security-Policy"; content = csp }
                meta(name = "referrer", content = "strict-origin-when-cross-origin")
                meta(name = "theme-color", content = "#07090b")
                meta(name = "color-scheme", content = "dark")
                link(rel = "apple-touch-icon", href = "/apple-touch-icon.png")
                link(rel = "manifest", href = "/site.webmanifest")
                link(rel = "preload", href = "/fonts/big-shoulders-display.woff2") {
                    attributes["as"] = "font"; attributes["type"] = "font/woff2"; attributes["crossorigin"] = ""
                }
                link(rel = "preload", href = "/fonts/jetbrains-mono.woff2") {
                    attributes["as"] = "font"; attributes["type"] = "font/woff2"; attributes["crossorigin"] = ""
                }
                // Palette, glow and texture strengths first; hud.css only reads them
                link(rel = "stylesheet", href = "/css/theme.css")
                link(rel = "stylesheet", href = "/css/hud.css")
                // Decides on the boot intro before first paint (source: src/head/boot.js)
                script { unsafe { raw(boot) } }
            }
        }
        export {
            // Source maps would ship the full Kotlin source to every visitor
            includeSourceMap.set(false)
            operationSlugs.forEach { addExtraRoute("/operations/$it") }
        }
    }
}

val generateSitemap = tasks.register("generateSitemap") {
    val outDir = layout.buildDirectory.dir("generated/seo")
    val routes = exportRoutes
    val base = siteUrl
    inputs.property("routes", routes)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("public/sitemap.xml").asFile
        file.parentFile.mkdirs()
        file.writeText(buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
            routes.forEach { appendLine("  <url><loc>$base$it</loc></url>") }
            appendLine("</urlset>")
        })
    }
}

/** The one CV file. scripts/update-cv.sh replaces it; everything else about the CV is derived from it. */
val cvSource = layout.projectDirectory.file("src/jsMain/resources/public/cv/Sebabatso_Moleele_CV.pdf")

/** Publishes the CV again at the short, stable address /cv.pdf, so there is still only one file to replace. */
val publishCvShortLink = tasks.register("publishCvShortLink") {
    val pdf = cvSource.asFile
    val outDir = layout.buildDirectory.dir("generated/cv-public")
    inputs.file(pdf).withPropertyName("cv")
    outputs.dir(outDir)
    doLast {
        check(pdf.isFile) { "CV not found: $pdf. Add it with ./scripts/update-cv.sh <path/to/cv.pdf>" }
        val target = outDir.get().file("public/cv.pdf").asFile
        target.parentFile.mkdirs()
        pdf.copyTo(target, overwrite = true)
    }
}

/** Measures the CV at build time for the button's "PDF · 63 KB" label, so the size is never typed by hand. */
val generateCvMeta = tasks.register("generateCvMeta") {
    val pdf = cvSource.asFile
    val outDir = layout.buildDirectory.dir("generated/cv-meta")
    inputs.file(pdf).withPropertyName("cv")
    outputs.dir(outDir)
    doLast {
        check(pdf.isFile) { "CV not found: $pdf. Add it with ./scripts/update-cv.sh <path/to/cv.pdf>" }
        val bytes = pdf.length()
        val label = if (bytes >= 1024 * 1024) {
            "PDF · " + String.format(Locale.ROOT,"%.1f", bytes / (1024.0 * 1024.0)) + " MB"
        } else {
            "PDF · ${Math.round(bytes / 1024.0).coerceAtLeast(1)} KB"
        }
        val file = outDir.get().file("fyi/lehlanyaverse/data/CvMeta.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            "// Generated by the generateCvMeta Gradle task from the CV file. Do not edit.\n" +
                "package fyi.lehlanyaverse.data\n\n" +
                "internal const val CV_SIZE_LABEL = \"$label\"\n" +
                "internal const val CV_SIZE_BYTES = ${bytes}L\n"
        )
    }
}

/**
 * Lists every `TODO(sebabatso)` left in Intel.kt in the build output, so missing content (HackerRank levels and
 * verify links, project repos, …) isn't forgotten. On GitHub Actions they also show as warnings on the run.
 * Never fails the build: a TODO renders as an absence on the site, not a broken link.
 */
val reportIntelTodos = tasks.register("reportIntelTodos") {
    val intel = intelFile.asFile
    val onCi = providers.environmentVariable("GITHUB_ACTIONS").map { it == "true" }.orElse(false)
    inputs.file(intel).withPropertyName("intel")
    outputs.upToDateWhen { false } // always report
    doLast {
        val todos = intel.readLines().withIndex()
            .filter { (_, line) -> "TODO(sebabatso)" in line }
            .map { (i, line) -> (i + 1) to line.substringAfter("TODO(sebabatso)").trimStart(':', ' ') }
        if (todos.isEmpty()) return@doLast
        val ci = onCi.get()
        println("Intel.kt: ${todos.size} TODO(sebabatso) left to fill in")
        todos.forEach { (line, text) ->
            if (ci) println("::warning file=site/src/jsMain/kotlin/fyi/lehlanyaverse/data/Intel.kt,line=$line::TODO $text")
            else println("  w: Intel.kt:$line  $text")
        }
    }
}

tasks.matching { it.name == "jsProcessResources" }.configureEach { dependsOn(reportIntelTodos) }

kotlin {
    configAsKobwebApplication("lehlanyaverse")

    // Webpack's default dev devtool wraps modules in eval(), which the strict CSP (no 'unsafe-eval') blocks.
    // Plain source maps keep dev, export snapshots and production under the same policy.
    js {
        browser {
            commonWebpackConfig {
                devtool = org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevtool.SOURCE_MAP
            }
        }
        // Data tests (src/jsTest) run on Node: fast and headless, no browser download needed
        nodejs()
    }

    sourceSets {
        jsMain {
            resources.srcDir(generateSitemap)
            resources.srcDir(publishCvShortLink)
            kotlin.srcDir(generateCvMeta)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.html.core)
                implementation(libs.kobweb.core)
                // Smooth wheel scrolling (components/fx/SmoothScroll.kt). Exact pin, bundled into the app JS.
                implementation(npm("lenis", "1.3.26"))
            }
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
