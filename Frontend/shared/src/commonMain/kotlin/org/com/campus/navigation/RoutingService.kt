package org.com.campus.navigation

interface RoutingService {
    suspend fun calculateRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Route
}
