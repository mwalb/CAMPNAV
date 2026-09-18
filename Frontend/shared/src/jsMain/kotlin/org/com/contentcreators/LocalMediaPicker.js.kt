package org.com.creator.components

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import kotlin.random.Random
import org.com.creator.model.MediaItem
import org.com.creator.model.MediaType

@Composable
actual fun LocalMediaPicker(
    onMediaSelected: (MediaItem) -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit
) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = "image/*,video/*"
    
    input.onchange = {
        val file = input.files?.item(0)
        if (file != null) {
            val url = URL.createObjectURL(file)
            onMediaSelected(
                MediaItem(
                    id = Random.nextLong().toString(),
                    name = file.name,
                    type = if (file.type.contains("video")) MediaType.VIDEO else MediaType.IMAGE,
                    uri = url
                )
            )
        }
    }

    trigger {
        input.click()
    }
}
