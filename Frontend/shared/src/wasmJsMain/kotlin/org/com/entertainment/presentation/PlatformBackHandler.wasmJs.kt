@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com.entertainment.presentation

import kotlin.js.ExperimentalWasmJsInterop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@JsFun("""(callback) => {
    const handler = (e) => callback();
    try {
        window.history.pushState(null, '', window.location.href);
        window.addEventListener('popstate', handler);
    } catch(err) {}
    return () => {
        try { window.removeEventListener('popstate', handler); } catch(err) {}
    };
}""")
private external fun jsRegisterBackHandler(callback: () -> Unit): () -> Unit

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (enabled) {
        DisposableEffect(Unit) {
            val unregister = jsRegisterBackHandler(onBack)
            onDispose {
                unregister()
            }
        }
    }
}
