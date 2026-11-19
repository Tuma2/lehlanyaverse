package fyi.lehlanyaverse.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import fyi.lehlanyaverse.components.fx.Fx
import fyi.lehlanyaverse.data.Intel
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.math.pow
import kotlin.math.roundToInt

/** [FULL]: label, subline and VIEW link (hero, COMMS, maintenance). [COMPACT]: bracketed icon + "CV" (header). */
enum class DossierVariant { FULL, COMPACT }

private enum class Phase(val attr: String) { IDLE("idle"), DECRYPT("decrypt"), DONE("done") }

/** The counter runs this long, then holds a beat at 100% before "extracted": under 800ms in all. */
private const val DECRYPT_MS = 450.0
private const val DONE_AT_MS = 520L
private const val HOLD_MS = 2500L

/**
 * EXTRACT DOSSIER: downloads the CV ([fyi.lehlanyaverse.data.CvFile], in Intel.cv). A real `<a download>`, so it
 * works without the app, and phones that ignore `download` open the PDF instead.
 *
 * A click plays a short "decrypt" on the button itself. It never cancels the click, so the download starts at
 * once and the animation only decorates it. Without motion it goes straight to "DOSSIER EXTRACTED ✓".
 * Nothing is reported anywhere: the site has no analytics.
 */
@Composable
fun Dossier(variant: DossierVariant = DossierVariant.FULL) {
    val cv = Intel.cv
    val compact = variant == DossierVariant.COMPACT
    var run by remember { mutableStateOf(0) }
    var phase by remember { mutableStateOf(Phase.IDLE) }
    var pct by remember { mutableStateOf(0) }

    // Keyed on the click count: a second click cancels the running sequence and starts it over
    LaunchedEffect(run) {
        if (run == 0) return@LaunchedEffect
        if (Fx.motion) {
            phase = Phase.DECRYPT
            pct = 0
            val start = withFrameMillis { it }
            while (pct < 100) {
                val t = ((withFrameMillis { it } - start) / DECRYPT_MS).coerceIn(0.0, 1.0)
                pct = ((1 - (1 - t).pow(3)) * 100).roundToInt()
            }
            delay((DONE_AT_MS - (withFrameMillis { it } - start)).coerceAtLeast(0))
        }
        phase = Phase.DONE
        delay(HOLD_MS)
        phase = Phase.IDLE
    }

    val sizeWords = cv.size.replace(" · ", ", ")
    val link = @Composable {
        A(cv.href, {
            classes(*listOfNotNull("dossier__btn", "dossier--compact".takeIf { compact }).toTypedArray())
            attr("download", cv.fileName)
            attr("aria-label", "Download CV ($sizeWords)")
            attr("data-lock", if (compact) "EXTRACT CV" else "EXTRACT DOSSIER") // cursor reticle (fx/Reticle.kt)
            attr("data-state", phase.attr)
            onClick { run++ }
        }) {
            Deco("dossier__icon") { Span({ classes("dossier__arrow") }) { Text("↓") } }
            if (compact) {
                // Stacked in one grid cell (CSS shows the current one), so the width never jumps
                Deco("dossier__states", "dossier__cv") {
                    Span({ classes("dossier__state", "dossier__state--idle") }) { Text("CV") }
                    Span({ classes("dossier__state", "dossier__state--decrypt") }) { Text("${pct.toString().padStart(2, '0')}%") }
                    Span({ classes("dossier__state", "dossier__state--done") }) { Text("✓") }
                }
            } else {
                Deco("dossier__text") {
                    Span({ classes("dossier__states", "dossier__label") }) {
                        Span({ classes("dossier__state", "dossier__state--idle") }) { Text("Extract dossier") }
                        Span({ classes("dossier__state", "dossier__state--decrypt") }) {
                            Text("Decrypting… $pct%")
                        }
                        Span({ classes("dossier__state", "dossier__state--done") }) { Text("Dossier extracted ✓") }
                    }
                    Span({ classes("dossier__sub") }) { Text("CV · ${cv.size.substringBefore(" ·")} · Updated ${monthLabel(cv.updated)}") }
                }
            }
            // Progress line along the foot: scaled, never resized
            Deco("dossier__bar") { if (phase == Phase.DECRYPT) Span({ style { property("transform", "scaleX(${pct / 100.0})") } }) }
        }
    }

    if (compact) {
        Span({ classes("dossier", "dossier--hdr") }) {
            link()
            Span({ classes("sr-only"); attr("role", "status") }) { if (phase == Phase.DONE) Text("CV download started") }
        }
        return
    }
    Div({ classes("dossier") }) {
        link()
        A(cv.href, {
            classes("dossier__view")
            attr("target", "_blank")
            attr("rel", "noopener")
            attr("aria-label", "View CV in new tab")
            attr("data-lock", "VIEW FILE")
        }) {
            Text("View")
            Span({ attr("aria-hidden", "true") }) { Text("↗") }
        }
        Span({ classes("sr-only"); attr("role", "status") }) { if (phase == Phase.DONE) Text("CV download started") }
    }
}
