package org.com

import org.w3c.dom.HTMLVideoElement

@JsModule("hls.js")
@JsNonModule
external class Hls {
    companion object {
        fun isSupported(): Boolean
    }
    fun loadSource(src: String)
    fun attachMedia(video: HTMLVideoElement)
    fun on(event: String, callback: (dynamic, dynamic) -> Unit)
    fun destroy()
}
