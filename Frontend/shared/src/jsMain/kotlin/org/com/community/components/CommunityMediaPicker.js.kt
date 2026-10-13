package org.com.community.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun CommunityMediaPicker(
    onMediaCaptured: (String) -> Unit,
    content: @Composable (captureImage: () -> Unit, pickGallery: () -> Unit) -> Unit
) {
    val currentOnCaptured by rememberUpdatedState(onMediaCaptured)

    LaunchedEffect(Unit) {
        initWebMediaPickerJS { uri ->
            currentOnCaptured(uri)
        }
    }

    content(
        { triggerWebCameraJS() },
        { triggerWebGalleryJS() }
    )
}

private fun initWebMediaPickerJS(onCaptured: (String) -> Unit) {
    val windowObj = kotlinx.browser.window.asDynamic()
    windowObj.onWebMediaCapturedJS = { uri: String ->
        onCaptured(uri)
    }
}

private fun triggerWebCameraJS() {
    js("""
        let input = document.getElementById("web-camera-input");
        if (!input) {
            input = document.createElement("input");
            input.id = "web-camera-input";
            input.type = "file";
            input.accept = "image/*";
            input.setAttribute("capture", "environment");
            input.style.display = "none";
            document.body.appendChild(input);
        }
        input.onchange = (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    if (window.onWebMediaCapturedJS) {
                        window.onWebMediaCapturedJS(event.target.result);
                    }
                };
                reader.readAsDataURL(file);
            }
            input.value = "";
        };
        input.click();
    """)
}

private fun triggerWebGalleryJS() {
    js("""
        let input = document.getElementById("web-gallery-input");
        if (!input) {
            input = document.createElement("input");
            input.id = "web-gallery-input";
            input.type = "file";
            input.accept = "image/*";
            input.style.display = "none";
            document.body.appendChild(input);
        }
        input.onchange = (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    if (window.onWebMediaCapturedJS) {
                        window.onWebMediaCapturedJS(event.target.result);
                    }
                };
                reader.readAsDataURL(file);
            }
            input.value = "";
        };
        input.click();
    """)
}
