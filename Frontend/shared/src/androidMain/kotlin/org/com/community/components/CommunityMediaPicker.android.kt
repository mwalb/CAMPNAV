package org.com.community.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun CommunityMediaPicker(
    onMediaCaptured: (String) -> Unit,
    content: @Composable (captureImage: () -> Unit, pickGallery: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onMediaCaptured(it.toString()) }
    }

    // Camera Launcher
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { onMediaCaptured(it.toString()) }
        }
    }

    val triggerCamera = {
        val file = File(context.cacheDir, "captured_issue_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    val triggerGallery = {
        galleryLauncher.launch("image/*")
    }

    content(triggerCamera, triggerGallery)
}
