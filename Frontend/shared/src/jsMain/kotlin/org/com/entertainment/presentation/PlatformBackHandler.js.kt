package org.com.entertainment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (enabled) {
        DisposableEffect(Unit) {
            val listener: (Event) -> Unit = {
                onBack()
            }
            try {
                window.history.pushState(null, "", window.location.href)
                window.addEventListener("popstate", listener)
            } catch (e: Exception) {
                // ignore if history restricted
            }
            onDispose {
                try {
                    window.removeEventListener("popstate", listener)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }
}
