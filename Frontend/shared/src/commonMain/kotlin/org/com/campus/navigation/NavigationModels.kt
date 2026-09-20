package org.com.campus.navigation

import kotlinx.serialization.Serializable
import org.com.campus.data.CampusLocation

@Serializable
data class RoutePoint(val latitude: Double, val longitude: Double)

enum class NavigationStatus {
    IDLE,
    SHOWING_NAV_CHOICE,
    SELECTING_START_POINT,
    CONFIRMING_START_POINT,
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
data class NavigationStep(
    val instruction: String,
    val maneuver: String? = null,
    val distanceMeters: Double = 0.0,
    val startLocation: RoutePoint,
    val endLocation: RoutePoint
)

@Serializable
data class NavigationState(
    val status: NavigationStatus = NavigationStatus.IDLE,
    val origin: CampusLocation? = null,
    val originLatLng: RoutePoint? = null,
    val destination: CampusLocation? = null,
    val selectedStartPoint: RoutePoint? = null,
    val route: Route? = null,
    val userLocation: RoutePoint? = null,
    val userHeading: Double? = null,
    val isFollowingUser: Boolean = true,
    val currentStepIndex: Int = 0,
    val currentStep: NavigationStep? = null,
    val nextStep: NavigationStep? = null,
    val distanceToNextManeuver: Double = 0.0,
    val remainingDistance: Double = 0.0,
    val remainingDurationSeconds: Double = 0.0,
    val eta: String? = null,
    val voiceEnabled: Boolean = true,
    val isOffRoute: Boolean = false,
    val hasOffRoadFinalApproach: Boolean = false,
    val error: String? = null
)
