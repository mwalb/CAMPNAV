package org.com.campus.utils

actual fun openExternalMap(latitude: Double, longitude: Double, label: String) {
    val url = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    openUrl(url)
}

actual fun openGoogleMapsNavigation(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double
) {
    val url = "https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=driving"
    openUrl(url)
}

private fun openUrl(url: String): Unit = js("window.open(url, '_blank')")
