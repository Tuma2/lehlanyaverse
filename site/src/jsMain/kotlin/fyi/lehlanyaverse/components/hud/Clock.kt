package fyi.lehlanyaverse.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date

/** Live SAST time, "hh:mm:ss", ticking each second. Null in the static export: a timestamp is never baked in. */
@Composable
fun rememberSastTime(): String? {
    var now by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        if (AppGlobals.isExporting) return@LaunchedEffect
        while (true) {
            now = sastNow()
            delay(1000)
        }
    }
    return now
}

/**
 * Surveillance-feed overlay, "● CAM 03 // 02:14:07". Decorative (a per-second update would be noise for screen
 * readers). Position it with CSS on the element it sits over.
 */
@Composable
fun CamStamp(cam: String, vararg cls: String) {
    val time = rememberSastTime()
    Span({ classes("cam", *cls); attr("aria-hidden", "true") }) {
        Span({ classes("cam__rec") })
        Text("CAM $cam // ${time ?: "--:--:--"}")
    }
}

/** South Africa has no daylight saving: SAST is always UTC+2. */
private fun sastNow(): String {
    val d = Date(Date.now() + 2 * 60 * 60 * 1000)
    fun two(n: Int) = n.toString().padStart(2, '0')
    return "${two(d.getUTCHours())}:${two(d.getUTCMinutes())}:${two(d.getUTCSeconds())}"
}
