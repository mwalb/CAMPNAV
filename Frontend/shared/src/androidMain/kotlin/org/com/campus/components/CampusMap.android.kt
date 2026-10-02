package org.com.campus.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.com.campus.navigation.NavigationState
import org.com.campus.navigation.NavigationStatus
import org.com.campus.navigation.RoutePoint

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.com.campus.utils.isValidCoordinate
import org.com.campus.utils.openGoogleMapsNavigation

@SuppressLint("MissingPermission")
@Composable
actual fun CampusMap(
    modifier: Modifier,
    university: University,
    locations: List<CampusLocation>,
    allLocations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit,
    initialSelectedLocation: CampusLocation?,
    navigationState: NavigationState,
    onStartNavigation: (CampusLocation, CampusLocation?) -> Unit,
    onEndNavigation: () -> Unit,
    onLocationUpdate: (RoutePoint) -> Unit,
    onMapClick: (RoutePoint) -> Unit,
    onStatusChange: (NavigationStatus) -> Unit
) {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val initialPos = if (initialSelectedLocation != null) {
        LatLng(initialSelectedLocation.latitude, initialSelectedLocation.longitude)
    } else {
        LatLng(university.latitude ?: 0.0, university.longitude ?: 0.0)
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, if (initialSelectedLocation != null) 18f else (university.defaultZoom ?: 15f))
    }

    // Handle location updates
    LaunchedEffect(navigationState.status) {
        if (navigationState.status != NavigationStatus.IDLE) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let {
                        onLocationUpdate(RoutePoint(it.latitude, it.longitude))
                    }
                }
            }

            locationClient.requestLocationUpdates(locationRequest, callback, null)
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = navigationState.status != NavigationStatus.IDLE),
            uiSettings = MapUiSettings(zoomControlsEnabled = true),
            onMapClick = { latLng ->
                if (navigationState.status == NavigationStatus.SELECTING_START_POINT) {
                    navigationState.destination?.let { dest ->
                        println("[CAMPNAV] [DIAGNOSTIC] Manual origin selected")
                        println("[CAMPNAV] [DIAGNOSTIC] Origin latitude: ${latLng.latitude}, Origin longitude: ${latLng.longitude}")
                        println("[CAMPNAV] [DIAGNOSTIC] Navigation URL generated")
                        println("[CAMPNAV] [DIAGNOSTIC] Google Maps launch requested")
                        openGoogleMapsNavigation(latLng.latitude, latLng.longitude, dest.latitude, dest.longitude)
                        println("[CAMPNAV] [DIAGNOSTIC] Navigation session reset")
                    }
                    onEndNavigation()
                } else {
                    onMapClick(RoutePoint(latLng.latitude, latLng.longitude))
                }
            }
        ) {
            // Marker for selected start point
            navigationState.selectedStartPoint?.let { point ->
                Marker(
                    state = rememberMarkerState(position = LatLng(point.latitude, point.longitude)),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    title = "Selected Start Point"
                )
            }

            locations.forEach { location ->
                val markerState = rememberMarkerState(position = LatLng(location.latitude, location.longitude))
                
                LaunchedEffect(location.id, navigationState.destination?.id) {
                    if (location.id == navigationState.destination?.id) {
                        markerState.showInfoWindow()
                    }
                }

                Marker(
                    state = markerState,
                    title = location.name,
                    snippet = "Tap to navigate",
                    onInfoWindowClick = {
                        println("[CAMPNAV] [DIAGNOSTIC] Navigate button clicked via info window")
                        onStartNavigation(location, null)
                    },
                    onClick = {
                        onLocationSelected(location)
                        false
                    }
                )
            }

            // Draw route if active
            navigationState.route?.let { route ->
                Polyline(
                    points = route.points.map { LatLng(it.latitude, it.longitude) },
                    color = Color.Blue,
                    width = 10f
                )
            }
        }

        // Navigate button when a location is selected
        if (navigationState.status == NavigationStatus.IDLE && navigationState.destination != null) {
            ExtendedFloatingActionButton(
                onClick = {
                    println("[CAMPNAV] [DIAGNOSTIC] Navigate button clicked")
                    onStartNavigation(navigationState.destination, null)
                },
                icon = { Icon(Icons.Default.Navigation, contentDescription = null) },
                text = { Text("Navigate") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }

        // Navigation bottom panel for non-idle states
        if (navigationState.status != NavigationStatus.IDLE && navigationState.destination != null) {
            when (navigationState.status) {
                NavigationStatus.SHOWING_NAV_CHOICE -> {
                    println("[CAMPNAV] [DIAGNOSTIC] Starting-point popup opened")
                    AlertDialog(
                        onDismissRequest = {
                            println("[CAMPNAV] [DIAGNOSTIC] Navigation session reset")
                            onEndNavigation()
                        },
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("Navigate to", style = MaterialTheme.typography.bodyMedium)
                                Text(navigationState.destination.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Text("How would you like to start?", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        },
                        confirmButton = {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = {
                                        println("[CAMPNAV] [DIAGNOSTIC] Current location requested")
                                        val fallbackLat = university.latitude ?: -6.7801
                                        val fallbackLng = university.longitude ?: 39.2041
                                        locationClient.lastLocation.addOnSuccessListener { loc ->
                                            if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                                                println("[CAMPNAV] [DIAGNOSTIC] Current location received: lat=${loc.latitude}, lng=${loc.longitude}")
                                                println("[CAMPNAV] [DIAGNOSTIC] Navigation URL generated")
                                                println("[CAMPNAV] [DIAGNOSTIC] Google Maps launch requested")
                                                openGoogleMapsNavigation(loc.latitude, loc.longitude, navigationState.destination.latitude, navigationState.destination.longitude)
                                                println("[CAMPNAV] [DIAGNOSTIC] Navigation session reset")
                                                onEndNavigation()
                                            } else {
                                                println("[CAMPNAV] [DIAGNOSTIC] Current location fallback used")
                                                println("[CAMPNAV] [DIAGNOSTIC] Navigation URL generated")
                                                println("[CAMPNAV] [DIAGNOSTIC] Google Maps launch requested")
                                                openGoogleMapsNavigation(fallbackLat, fallbackLng, navigationState.destination.latitude, navigationState.destination.longitude)
                                                println("[CAMPNAV] [DIAGNOSTIC] Navigation session reset")
                                                onEndNavigation()
                                            }
                                        }.addOnFailureListener {
                                            println("[CAMPNAV] [DIAGNOSTIC] Current location failed, fallback used")
                                            println("[CAMPNAV] [DIAGNOSTIC] Navigation URL generated")
                                            println("[CAMPNAV] [DIAGNOSTIC] Google Maps launch requested")
                                            openGoogleMapsNavigation(fallbackLat, fallbackLng, navigationState.destination.latitude, navigationState.destination.longitude)
                                            println("[CAMPNAV] [DIAGNOSTIC] Navigation session reset")
                                            onEndNavigation()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = org.com.core.ui.theme.AppColorScheme.primary)
                                ) {
                                    Icon(Icons.Default.MyLocation, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Use my current location")
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        println("[CAMPNAV] [DIAGNOSTIC] Manual origin selected (Select starting point)")
                                        onStatusChange(NavigationStatus.SELECTING_START_POINT)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = org.com.core.ui.theme.AppColorScheme.primary)
                                ) {
                                    Icon(Icons.Default.PinDrop, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Select starting point")
                                }
                                TextButton(onClick = {
                                    println("[CAMPNAV] [DIAGNOSTIC] Navigation session reset")
                                    onEndNavigation()
                                }) {
                                    Text("Cancel", color = Color.Red)
                                }
                            }
                        }
                    )
                }

                NavigationStatus.SELECTING_START_POINT -> {
                    // Selecting start point overlay handled above in onMapClick or cards
                }
                else -> {}
            }
        }
    }
}
