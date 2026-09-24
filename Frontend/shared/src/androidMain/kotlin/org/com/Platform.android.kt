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