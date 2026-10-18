package org.com.campus.presentation

actual fun setHomeScreenAdEnabled(enabled: Boolean) {
    // Monetag ads are website-only; do nothing on Android.
}
