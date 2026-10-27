import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.unsafe
import java.security.MessageDigest
import java.util.Base64

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
