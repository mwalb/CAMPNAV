package org.com.entertainment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.com.entertainment.model.Channel
import org.com.entertainment.model.PlayerState

@Composable
expect fun PlatformVideoPlayer(
    channel: Channel,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onStateChanged: (PlayerState) -> Unit = {}
)
