@file:JsModule("lenis")
@file:JsNonModule

package fyi.lehlanyaverse.components.fx

/*
 * The slice of Lenis (https://github.com/darkroomengineering/lenis) this site uses. Only SmoothScroll talks to it.
 */

@JsName("default")
external class Lenis(options: LenisOptions = definedExternally) {
    val scroll: Double
    val limit: Double
    val progress: Double

    fun raf(time: Double)
    fun scrollTo(target: dynamic, options: LenisScrollToOptions = definedExternally)
    fun on(event: String, callback: (Lenis) -> Unit): () -> Unit
    fun start()
    fun stop()
    fun resize()
    fun destroy()
}

external interface LenisOptions {
    var lerp: Double?
    var smoothWheel: Boolean?
    var syncTouch: Boolean?
    var wheelMultiplier: Double?
    var autoRaf: Boolean?
}

external interface LenisScrollToOptions {
    var offset: Double?
    var duration: Double?
    var easing: ((Double) -> Double)?
    var lock: Boolean?
    var immediate: Boolean?
    var force: Boolean?
    var onComplete: ((Lenis) -> Unit)?
}
