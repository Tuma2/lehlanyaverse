plugins {
    // Load plugins once at the root so subprojects share a single classloader
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kobweb.application) apply false
}
