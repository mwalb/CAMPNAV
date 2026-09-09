package org.com.creator.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import org.com.creator.model.MediaItem
import org.com.creator.model.MediaType

@Composable
actual fun LocalMediaPicker(
    onMediaSelected: (MediaItem) -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onMediaSelected(
                MediaItem(
                    id = it.toString(),
                    name = "Local Media",
                    type = MediaType.IMAGE, // Simplified
                    uri = it.toString()
                )
            )
        }
    }

    trigger {
        launcher.launch("*/*")
    }
}
