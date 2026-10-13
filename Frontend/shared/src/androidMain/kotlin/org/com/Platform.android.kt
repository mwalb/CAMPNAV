package org.com

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getCurrentEpochMillis(): Long = System.currentTimeMillis()

actual fun formatEpochMillis(millis: Long): String {
    val date = Date(millis)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

actual fun getSavedThemeIsDark(): Boolean? {
    return try {
        val file = java.io.File(System.getProperty("java.io.tmpdir") ?: ".", "campnav_theme.txt")
        if (file.exists()) {
            when (file.readText().trim()) {
                "true" -> true
                "false" -> false
                else -> null
            }
        } else null
    } catch (e: Exception) {
        null
    }
}

actual fun saveThemeIsDark(isDark: Boolean) {
    try {
        val file = java.io.File(System.getProperty("java.io.tmpdir") ?: ".", "campnav_theme.txt")
        file.writeText(if (isDark) "true" else "false")
    } catch (e: Exception) {
        // ignore
    }
}
