package org.com.creator.components

import androidx.compose.runtime.Composable
import org.com.creator.model.MediaItem

@Composable
expect fun LocalMediaPicker(
    onMediaSelected: (MediaItem) -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit
)
