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
    val routingService = remember { GoogleRoutingService("AIzaSyBrLZm8FhK4Xn6baE12QbY2D4wtTyfzj3M") }
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

    // Filter locations: show only selected place if destination is set, otherwise show all active
    val displayLocations = remember(locations, navigationState.destination) {
        val dest = navigationState.destination
        if (dest != null) {
            listOf(dest)
        } else {
            locations
        }
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
        onStartNavigation = { dest, origin ->
            if (origin == null && navigationState.userLocation == null) {
                navigationState = navigationState.copy(destination = dest, status = NavigationStatus.SELECTING_ORIGIN)
            } else {
                calculateRoute(dest, origin, navigationState.userLocation)
            }
        },
        onEndNavigation = {
            navigationState = NavigationState()
        },
        onLocationUpdate = { point ->
            val prevLoc = navigationState.userLocation
            navigationState = navigationState.copy(userLocation = point)
            
            // Auto-start navigation if we were waiting for location
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
