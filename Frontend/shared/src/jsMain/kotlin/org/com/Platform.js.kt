package org.com

import web.navigator.navigator
import kotlin.js.Date

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun getCurrentEpochMillis(): Long = Date.now().toLong()

actual fun formatEpochMillis(millis: Long): String {
    val date = Date(millis.toDouble())
    val hours = date.getHours().toString().padStart(2, '0')
    val minutes = date.getMinutes().toString().padStart(2, '0')
    return "$hours:$minutes"
}

actual fun getSavedThemeIsDark(): Boolean? {
    return try {
        val valStr = kotlinx.browser.window.localStorage.getItem("campnav_theme_is_dark")
        when (valStr) {
            "true" -> true
            "false" -> false
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

actual fun saveThemeIsDark(isDark: Boolean) {
    try {
        kotlinx.browser.window.localStorage.setItem("campnav_theme_is_dark", if (isDark) "true" else "false")
    } catch (e: Exception) {
        // ignore
    }
}

actual fun getApiBaseUrl(): String {
    return try {
        val window = kotlinx.browser.window
        val customUrl = window.asDynamic().__CAMPNAV_API_URL__ as? String
        if (!customUrl.isNullOrBlank()) return customUrl

        val metaElement = kotlinx.browser.document.querySelector("meta[name='campnav-api-url']")
        val metaUrl = metaElement?.getAttribute("content")
        if (!metaUrl.isNullOrBlank()) return metaUrl

        val hostname = window.location.hostname
        if (hostname == "localhost" || hostname == "127.0.0.1") {
            "http://localhost:8080"
        } else {
            "https://campnav-1.fly.dev"
        }
    } catch (e: Exception) {
        "http://localhost:8080"
    }
}
