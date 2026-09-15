package org.com.campus.navigation

import kotlinx.serialization.Serializable
import org.com.campus.data.CampusLocation

@Serializable
data class RoutePoint(val latitude: Double, val longitude: Double)

enum class NavigationStatus {
    IDLE,
    SELECTING_ORIGIN,
    CALCULATING,
    ACTIVE,
    OFF_ROUTE,
    COMPLETED,
    ERROR
}

@Serializable
data class Route(
    val points: List<RoutePoint>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val origin: RoutePoint,
    val destination: RoutePoint,
    val status: String? = null
)

@Serializable
data class NavigationState(
    val status: NavigationStatus = NavigationStatus.IDLE,
    val origin: CampusLocation? = null,
    val originLatLng: RoutePoint? = null,
    val destination: CampusLocation? = null,
    val route: Route? = null,
    val userLocation: RoutePoint? = null,
    val isFollowingUser: Boolean = false,
    val error: String? = null
)
