package org.com.creator.components

import androidx.compose.runtime.Composable
import org.com.creator.model.MediaItem
import org.com.creator.model.MediaType

@Composable
actual fun LocalMediaPicker(
    onMediaSelected: (MediaItem) -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit
) {
    // Simplified for web
    trigger {
        onMediaSelected(
            MediaItem(
                id = "web_upload",
                name = "Web Media",
                type = MediaType.IMAGE,
                uri = "https://picsum.photos/400/300"
            )
        )
    }
}
