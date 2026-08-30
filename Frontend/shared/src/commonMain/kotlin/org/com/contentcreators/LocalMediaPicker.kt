package org.com.contentcreators

import androidx.compose.runtime.Composable

@Composable
expect fun LocalMediaPicker(
    onMediaSelected: (MediaItem) -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit
)
