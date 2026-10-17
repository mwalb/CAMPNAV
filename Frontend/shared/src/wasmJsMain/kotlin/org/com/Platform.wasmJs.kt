@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

actual fun getCurrentEpochMillis(): Long = jsDateNow().toLong()

@JsFun("(millis) => { const d = new Date(millis); return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0'); }")
private external fun jsFormatTime(millis: Double): String

actual fun formatEpochMillis(millis: Long): String = jsFormatTime(millis.toDouble())

@JsFun("""() => {
    try {
        const valStr = window.localStorage.getItem('campnav_theme_is_dark');
        if (valStr === 'false') return 0;
        if (valStr === 'true') return 1;
        return -1;
    } catch(e) {
        return -1;
    }
}""")
private external fun jsGetThemeIsDarkInt(): Int

@JsFun("""(isDark) => {
    try {
        window.localStorage.setItem('campnav_theme_is_dark', isDark ? 'true' : 'false');
    } catch(e) {}
}""")
private external fun jsSaveThemeIsDark(isDark: Boolean)

actual fun getSavedThemeIsDark(): Boolean? {
    return when (jsGetThemeIsDarkInt()) {
        1 -> true
        0 -> false
        else -> null
    }
}

actual fun saveThemeIsDark(isDark: Boolean) {
    jsSaveThemeIsDark(isDark)
}

@JsFun("""() => {
    try {
        if (window.__CAMPNAV_API_URL__) return window.__CAMPNAV_API_URL__;
        const meta = document.querySelector("meta[name='campnav-api-url']");
        if (meta && meta.content) return meta.content;
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            return 'http://localhost:8080';
        }
        return 'https://campnav-1.fly.dev';
    } catch(e) {
        return 'http://localhost:8080';
    }
}""")
private external fun jsGetApiBaseUrl(): String

actual fun getApiBaseUrl(): String = jsGetApiBaseUrl()
