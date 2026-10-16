package org.com.entertainment.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.document
import org.com.Hls
import org.com.entertainment.model.Channel
import org.com.entertainment.model.PlayerState
import org.w3c.dom.HTMLVideoElement

private fun jsSafePlay(element: HTMLVideoElement) {
    js("""
        if (element) {
            try {
                var promise = element.play();
                if (promise && typeof promise.then === 'function') {
                    promise.catch(function(err) {
                        if (err && (err.name === 'AbortError' || (err.message && err.message.indexOf('interrupted') !== -1))) {
                            // Ignore expected play interruption
                        } else if (err && err.name === 'NotAllowedError') {
                            console.warn('[CAMPNAV Player] Autoplay prevented by browser policy.');
                        } else {
                            console.warn('[CAMPNAV Player] Play request rejected:', err);
                        }
                    });
                }
            } catch(e) {}
        }
    """)
}

private fun jsSafePause(element: HTMLVideoElement) {
    js("""
        if (element) {
            try {
                element.pause();
            } catch(e) {}
        }
    """)
}

private fun jsAttachListeners(
    element: HTMLVideoElement,
    onBuffering: () -> Unit,
    onPlaying: () -> Unit,
    onError: (String) -> Unit
) {
    val windowObj = kotlinx.browser.window.asDynamic()
    windowObj.cbBuffering = onBuffering
    windowObj.cbPlaying = onPlaying
    windowObj.cbError = onError
    js("""
        if (element) {
            var onWaitingHandler = function() { if (window.cbBuffering) window.cbBuffering(); };
            var onPlayingHandler = function() { if (window.cbPlaying) window.cbPlaying(); };
            var onErrorHandler = function() { if (window.cbError) window.cbError('Failed to load video stream. Please check connection or select another channel.'); };

            element._onWaitingHandler = onWaitingHandler;
            element._onPlayingHandler = onPlayingHandler;
            element._onErrorHandler = onErrorHandler;

            element.addEventListener('waiting', onWaitingHandler);
            element.addEventListener('play', onPlayingHandler);
            element.addEventListener('playing', onPlayingHandler);
            element.addEventListener('canplay', onPlayingHandler);
            element.addEventListener('error', onErrorHandler);
        }
    """)
}

private fun jsDetachListeners(element: HTMLVideoElement) {
    js("""
        if (element) {
            if (element._onWaitingHandler) element.removeEventListener('waiting', element._onWaitingHandler);
            if (element._onPlayingHandler) {
                element.removeEventListener('play', element._onPlayingHandler);
                element.removeEventListener('playing', element._onPlayingHandler);
                element.removeEventListener('canplay', element._onPlayingHandler);
            }
            if (element._onErrorHandler) element.removeEventListener('error', element._onErrorHandler);
            element._onWaitingHandler = null;
            element._onPlayingHandler = null;
            element._onErrorHandler = null;
        }
    """)
}

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
            style.zIndex = "1"
            controls = true
            autoplay = false
            style.backgroundColor = "black"
            style.objectFit = "contain"
        }
    }

    val hls = remember {
        if (Hls.isSupported()) Hls() else null
    }

    DisposableEffect(channel.url) {
        onStateChanged(PlayerState(isBuffering = true, error = null))
        jsAttachListeners(
            videoElement,
            onBuffering = { onStateChanged(PlayerState(isBuffering = true, error = null)) },
            onPlaying = { onStateChanged(PlayerState(isBuffering = false, error = null)) },
            onError = { msg -> onStateChanged(PlayerState(isBuffering = false, error = msg)) }
        )
        document.body?.appendChild(videoElement)
        
        onDispose {
            jsSafePause(videoElement)
            jsDetachListeners(videoElement)
            try {
                hls?.destroy()
            } catch (_: Throwable) {}
            videoElement.removeAttribute("src")
            videoElement.load()
            videoElement.remove()
        }
    }

    LaunchedEffect(channel.url) {
        jsSafePause(videoElement)
        if (channel.url.contains(".m3u8") && hls != null) {
            try {
                hls.attachMedia(videoElement)
                hls.loadSource(channel.url)
            } catch (_: Throwable) {
                videoElement.src = channel.url
            }
        } else {
            videoElement.src = channel.url
        }

        if (isPlaying) {
            jsSafePlay(videoElement)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            jsSafePlay(videoElement)
        } else {
            jsSafePause(videoElement)
        }
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
