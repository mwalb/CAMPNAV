package org.com

import org.w3c.dom.HTMLVideoElement

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
