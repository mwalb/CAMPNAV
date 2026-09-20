package org.com.campus.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.com.campus.components.CampusMap
import org.com.campus.data.CampusLocation
import org.com.campus.data.CampusRepository
import org.com.campus.data.University
import org.com.campus.navigation.*
import org.com.campus.utils.openGoogleMapsNavigation

@Composable
fun CampusMapScreen(
    university: University,
    initialSelectedLocation: CampusLocation? = null,
    selectedCategoryId: Long? = null,
    onBack: () -> Unit
) {
    var locations by remember { mutableStateOf(emptyList<CampusLocation>()) }
    var navigationState by remember { mutableStateOf(NavigationState()) }
    val repository = remember { CampusRepository() }
    val routingService = remember { GoogleRoutingService("AIzaSyAvna2gdB_NvFXsBGgY-WBnatqy9IKUN5s") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(university.id) {
        try {
            val fetchedLocations = repository.getLocations(university.id)
            locations = fetchedLocations.filter { it.isActive }
        } catch (e: Exception) {
            println("[CAMPNAV] Error: ${e.message}")
        }
    }

    // Handle initial state and navigation flag (1001L)
    LaunchedEffect(initialSelectedLocation, selectedCategoryId) {
        if (initialSelectedLocation != null) {
            if (selectedCategoryId == 1001L) {
                navigationState = navigationState.copy(
                    destination = initialSelectedLocation,
                    status = NavigationStatus.SELECTING_ORIGIN
                )
            } else {
                navigationState = navigationState.copy(
                    destination = initialSelectedLocation,
                    status = NavigationStatus.IDLE
                )
            }
        }
    }

    fun calculateRoute(dest: CampusLocation, origin: CampusLocation?, userLoc: RoutePoint?) {
        scope.launch {
            navigationState = navigationState.copy(status = NavigationStatus.CALCULATING, origin = origin, originLatLng = userLoc)
            try {
                val startLat = origin?.latitude ?: userLoc?.latitude ?: throw Exception("Origin missing")
                val startLng = origin?.longitude ?: userLoc?.longitude ?: throw Exception("Origin missing")
                
                val route = routingService.calculateRoute(startLat, startLng, dest.latitude, dest.longitude)
                navigationState = navigationState.copy(
                    status = NavigationStatus.ACTIVE,
                    route = route,
                    originLatLng = RoutePoint(startLat, startLng)
                )
            } catch (e: Exception) {
                navigationState = navigationState.copy(status = NavigationStatus.ERROR, error = e.message)
            }
        }
    }

    // Filter locations: show all active locations to keep markers constant
    val displayLocations = remember(locations) {
        locations
    }

    CampusMap(
        modifier = Modifier.fillMaxSize(),
        university = university,
        locations = displayLocations,
        onLocationSelected = { location ->
            navigationState = navigationState.copy(
                destination = location,
                status = NavigationStatus.IDLE
            )
        },
        onBack = onBack,
        initialSelectedLocation = initialSelectedLocation,
        navigationState = navigationState,
        onStartNavigation = { dest, _ ->
            navigationState = navigationState.copy(
                destination = dest,
                status = NavigationStatus.SHOWING_NAV_CHOICE
            )
        },
        onEndNavigation = {
            navigationState = NavigationState()
        },
        onLocationUpdate = { point ->
            val prevLoc = navigationState.userLocation
            navigationState = navigationState.copy(userLocation = point)
            
            // Auto-start navigation if we were waiting for location (internal routing only)
            if (navigationState.status == NavigationStatus.SELECTING_ORIGIN && prevLoc == null) {
                navigationState.destination?.let { dest ->
                    calculateRoute(dest, null, point)
                }
            }

            // Off-route detection and recalculation
            val currentRoute = navigationState.route
            if (navigationState.status == NavigationStatus.ACTIVE && currentRoute != null && navigationState.origin == null) {
                val lastOrigin = navigationState.originLatLng
                if (lastOrigin != null) {
                    val dist = calculateDistance(point.latitude, point.longitude, lastOrigin.latitude, lastOrigin.longitude)
                    if (dist > 30) { // 30 meters threshold
                        calculateRoute(navigationState.destination!!, null, point)
                    }
                }
            }
        },
        onMapClick = { point ->
            if (navigationState.status == NavigationStatus.SELECTING_START_POINT) {
                navigationState.destination?.let { dest ->
                    // Directly open Google Maps navigation without extra popup
                    openGoogleMapsNavigation(point.latitude, point.longitude, dest.latitude, dest.longitude)
                }
                // Return to idle state after starting navigation
                navigationState = NavigationState()
            }
        },
        onStatusChange = { status ->
            navigationState = navigationState.copy(status = status)
        }
    )
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3
    val phi1 = lat1 * kotlin.math.PI / 180
    val phi2 = lat2 * kotlin.math.PI / 180
    val dphi = (lat2 - lat1) * kotlin.math.PI / 180
    val dlambda = (lon2 - lon1) * kotlin.math.PI / 180
    val a = kotlin.math.sin(dphi / 2) * kotlin.math.sin(dphi / 2) +
            kotlin.math.cos(phi1) * kotlin.math.cos(phi2) *
            kotlin.math.sin(dlambda / 2) * kotlin.math.sin(dlambda / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return r * c
}
