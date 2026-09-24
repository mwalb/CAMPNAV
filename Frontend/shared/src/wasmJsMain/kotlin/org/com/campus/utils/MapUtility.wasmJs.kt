@file:OptIn(ExperimentalWasmJsInterop::class)
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

actual fun getCurrentUserLocation(onLocationReceived: (Double, Double) -> Unit) {
    jsFetchGeoLocation(onLocationReceived)
}

private fun openUrl(url: String): Unit = js("window.open(url, '_blank')")

@JsFun("""(onSelected) => {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (pos) => { onSelected(pos.coords.latitude, pos.coords.longitude); },
            (err) => { onSelected(-6.7824, 39.2083); }
        );
    } else {
        onSelected(-6.7824, 39.2083);
    }
}""")
private external fun jsFetchGeoLocation(onSelected: (Double, Double) -> Unit)
