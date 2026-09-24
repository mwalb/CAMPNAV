package org.com.community.components

import androidx.compose.runtime.Composable

@Composable
actual fun CommunityMediaPicker(
    onMediaCaptured: (String) -> Unit,
    content: @Composable (captureImage: () -> Unit, pickGallery: () -> Unit) -> Unit
) {
    content({}, {})
}
