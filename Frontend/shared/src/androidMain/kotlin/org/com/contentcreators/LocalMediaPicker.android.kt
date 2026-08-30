package org.com.contentcreators

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import kotlin.random.Random

@Composable
actual fun LocalMediaPicker(
    onMediaSelected: (MediaItem) -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onMediaSelected(
                MediaItem(
                    id = Random.nextLong().toString(),
                    name = "Local Media",
                    type = if (it.toString().contains("video")) MediaType.VIDEO else MediaType.IMAGE,
                    uri = it.toString()
                )
            )
        }
    }

    trigger {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }
}
