@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

actual fun getCurrentEpochMillis(): Long = jsDateNow().toLong()

@JsFun("(millis) => { const d = new Date(millis); return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0'); }")
private external fun jsFormatTime(millis: Double): String

actual fun formatEpochMillis(millis: Long): String = jsFormatTime(millis.toDouble())