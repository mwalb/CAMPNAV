package org.com.campus.utils

import kotlinx.browser.window

actual fun openExternalMap(latitude: Double, longitude: Double, label: String) {
    val url = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    window.open(url, "_blank")
}

actual fun openGoogleMapsNavigation(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double
) {
    val url = "https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=driving"
    window.open(url, "_blank")
}

actual fun getCurrentUserLocation(onLocationReceived: (Double, Double) -> Unit) {
    js("""
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(pos) { onLocationReceived(pos.coords.latitude, pos.coords.longitude); },
                function(err) { onLocationReceived(-6.7824, 39.2083); }
            );
        } else {
            onLocationReceived(-6.7824, 39.2083);
        }
    """)
}
