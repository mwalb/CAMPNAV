package org.com.campus.utils

expect fun openExternalMap(latitude: Double, longitude: Double, label: String)

expect fun openGoogleMapsNavigation(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double
)

expect fun getCurrentUserLocation(onLocationReceived: (Double, Double) -> Unit)

fun isValidCoordinate(lat: Double, lng: Double): Boolean {
    return lat.isFinite() && lat in -90.0..90.0 &&
           lng.isFinite() && lng in -180.0..180.0
}
