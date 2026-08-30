package org.com.contentcreators

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import kotlin.random.Random
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
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
            val url = webUrlCreateObjectURL(file)
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

// URL.createObjectURL helper for WasmJs
@OptIn(ExperimentalWasmJsInterop::class)
private fun webUrlCreateObjectURL(obj: JsAny): String = js("URL.createObjectURL(obj)")
