package org.com.entertainment.data

import org.w3c.dom.HTMLVideoElement
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsModule("hls.js")
external class Hls() {
    companion object {
        fun isSupported(): Boolean
    }
    fun loadSource(src: String)
    fun attachMedia(video: HTMLVideoElement)
    fun on(event: String, callback: JsFunction)
    fun destroy()
}

external interface JsFunction : JsAny
