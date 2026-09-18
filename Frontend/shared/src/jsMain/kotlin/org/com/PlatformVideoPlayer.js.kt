package org.com.entertainment.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.positionInWindow
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement
import org.com.entertainment.model.Channel
import org.com.entertainment.model.PlayerState
import org.com.Hls

@Composable
actual fun PlatformVideoPlayer(
    channel: Channel,
    isPlaying: Boolean,
    modifier: Modifier,
    onStateChanged: (PlayerState) -> Unit
) {
    val density = LocalDensity.current
    
    val videoElement = remember {
        (document.createElement("video") as HTMLVideoElement).apply {
            style.position = "absolute"
            style.zIndex = "10"
            controls = true
            autoplay = true
            style.backgroundColor = "black"
        }
    }

    val hls = remember {
        if (Hls.isSupported()) Hls() else null
    }

    DisposableEffect(Unit) {
        document.body?.appendChild(videoElement)
        onDispose {
            hls?.destroy()
            videoElement.remove()
        }
    }

    LaunchedEffect(channel.url) {
        if (channel.url.contains(".m3u8") && hls != null) {
            hls.attachMedia(videoElement)
            hls.loadSource(channel.url)
        } else {
            videoElement.src = channel.url
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) videoElement.play() else videoElement.pause()
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                videoElement.style.left = "${position.x / density.density}px"
                videoElement.style.top = "${position.y / density.density}px"
                videoElement.style.width = "${size.width / density.density}px"
                videoElement.style.height = "${size.height / density.density}px"
            }
    )
}
