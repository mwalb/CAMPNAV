@file:OptIn(ExperimentalWasmJsInterop::class)
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
        onWebMediaCapturedInternal = { uri ->
            currentOnCaptured(uri)
        }
        initWebMediaPickerBridge()
    }

    content(
        { triggerWebCamera() },
        { triggerWebGallery() }
    )
}

private var onWebMediaCapturedInternal: (String) -> Unit = {}

@JsFun("""() => {
    window.handleWebMediaCaptured = (uri) => {
        if (window.onWebMediaCapturedJS) {
            window.onWebMediaCapturedJS(uri);
        }
    };
}""")
private external fun initWebMediaPickerBridge()

@JsFun("""() => {
    let input = document.getElementById("web-camera-input");
    if (!input) {
        input = document.createElement("input");
        input.id = "web-camera-input";
        input.type = "file";
        input.accept = "image/jpeg,image/png,image/webp,image/gif";
        input.setAttribute("capture", "environment");
        input.style.display = "none";
        document.body.appendChild(input);
    }
    input.onchange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (!file.type.startsWith('image/')) {
                alert('Invalid file format. Please select an image file.');
                input.value = '';
                return;
            }
            if (file.size > 10 * 1024 * 1024) {
                alert('Image size exceeds 10MB limit. Please choose a smaller file.');
                input.value = '';
                return;
            }
            const reader = new FileReader();
            reader.onload = (event) => {
                if (window.handleWebMediaCaptured) {
                    window.handleWebMediaCaptured(event.target.result);
                }
            };
            reader.onerror = () => {
                alert('Failed to read image file.');
            };
            reader.readAsDataURL(file);
        }
        input.value = "";
    };
    input.click();
}""")
private external fun triggerWebCamera()

@JsFun("""() => {
    let input = document.getElementById("web-gallery-input");
    if (!input) {
        input = document.createElement("input");
        input.id = "web-gallery-input";
        input.type = "file";
        input.accept = "image/jpeg,image/png,image/webp,image/gif";
        input.style.display = "none";
        document.body.appendChild(input);
    }
    input.onchange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (!file.type.startsWith('image/')) {
                alert('Invalid file format. Please select an image file.');
                input.value = '';
                return;
            }
            if (file.size > 10 * 1024 * 1024) {
                alert('Image size exceeds 10MB limit. Please choose a smaller file.');
                input.value = '';
                return;
            }
            const reader = new FileReader();
            reader.onload = (event) => {
                if (window.handleWebMediaCaptured) {
                    window.handleWebMediaCaptured(event.target.result);
                }
            };
            reader.onerror = () => {
                alert('Failed to read image file.');
            };
            reader.readAsDataURL(file);
        }
        input.value = "";
    };
    input.click();
}""")
private external fun triggerWebGallery()
