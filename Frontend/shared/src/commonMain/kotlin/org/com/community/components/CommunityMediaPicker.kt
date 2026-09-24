package org.com.community.components

import androidx.compose.runtime.Composable

@Composable
expect fun CommunityMediaPicker(
    onMediaCaptured: (String) -> Unit, // Returns URI or Base64
    content: @Composable (captureImage: () -> Unit, pickGallery: () -> Unit) -> Unit
)
