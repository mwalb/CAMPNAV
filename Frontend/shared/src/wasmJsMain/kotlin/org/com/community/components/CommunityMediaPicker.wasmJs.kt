package org.com.community.components

import androidx.compose.runtime.Composable

@Composable
actual fun CommunityMediaPicker(
    onMediaCaptured: (String) -> Unit,
    content: @Composable (captureImage: () -> Unit, pickGallery: () -> Unit) -> Unit
) {
    // For Web, capture and gallery often use the same input[type=file] with different capture attributes
    // This is a placeholder for web implementation
    content({}, {})
}
