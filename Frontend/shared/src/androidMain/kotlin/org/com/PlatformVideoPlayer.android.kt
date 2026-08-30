package org.com

import android.app.Activity
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.platform.LocalContext

@OptIn(UnstableApi::class)
@Composable
actual fun PlatformVideoPlayer(
    channel: Channel,
    isPlaying: Boolean,
    modifier: Modifier,
    onStateChanged: (PlayerState) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build().apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            setAudioAttributes(audioAttributes, true)
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    onStateChanged(
                        PlayerState(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            error = null
                        )
                    )
                }

                override fun onPlayerError(error: PlaybackException) {
                    onStateChanged(
                        PlayerState(
                            isBuffering = false,
                            error = "Error: ${error.message}"
                        )
                    )
                }
            })
        }
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            exoPlayer.release()
        }
    }

    LaunchedEffect(channel.url) {
        val mediaItem = MediaItem.Builder()
            .setUri(channel.url)
            .setMimeType(if (channel.url.contains(".m3u8")) MimeTypes.APPLICATION_M3U8 else null)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
    }

    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            }
        },
        modifier = modifier
    )
}
