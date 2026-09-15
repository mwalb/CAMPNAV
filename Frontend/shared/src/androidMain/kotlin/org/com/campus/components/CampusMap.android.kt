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

@SuppressLint("MissingPermission")
@Composable
actual fun CampusMap(
    modifier: Modifier,
    university: University,
    locations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit,
    initialSelectedLocation: CampusLocation?,
    navigationState: NavigationState,
    onStartNavigation: (CampusLocation, CampusLocation?) -> Unit,
    onEndNavigation: () -> Unit,
    onLocationUpdate: (RoutePoint) -> Unit
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
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
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
                onClick = { onStartNavigation(navigationState.destination, null) },
                icon = { Icon(Icons.Default.Navigation, contentDescription = null) },
                text = { Text("Navigate") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }

        // Navigation bottom panel for non-idle states
        if (navigationState.status != NavigationStatus.IDLE && navigationState.destination != null) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    when (navigationState.status) {
                        NavigationStatus.SELECTING_ORIGIN -> {
                            Text(
                                text = "Navigate to ${navigationState.destination.name}",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            
                            androidx.compose.material3.Button(
                                onClick = {
                                    onStartNavigation(navigationState.destination, null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = org.com.core.ui.theme.AppColorScheme.primary)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                                Text(if (navigationState.userLocation != null) "Use Current Location" else "Waiting for Current Location...")
                            }
                            
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            Text("Or select a starting location:", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = Color.Gray)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            var selectedOrigin by remember { mutableStateOf<CampusLocation?>(null) }
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedOrigin?.name ?: "Choose Starting Location")
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    locations.filter { it.id != navigationState.destination.id }.forEach { loc ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(loc.name) },
                                            onClick = {
                                                selectedOrigin = loc
                                                expanded = false
                                                onStartNavigation(navigationState.destination, loc)
                                            }
                                        )
                                    }
                                }
                            }
                            
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.TextButton(
                                onClick = { onEndNavigation() },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Cancel", color = Color.Red)
                            }
                        }
                        NavigationStatus.CALCULATING -> {
                            Text("Calculating route to ${navigationState.destination.name}...", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = org.com.core.ui.theme.AppColorScheme.primary
                            )
                        }
                        NavigationStatus.ACTIVE -> {
                            Text(
                                text = "Routing to ${navigationState.destination.name}",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                            navigationState.route?.let { r ->
                                val distText = if (r.distanceMeters >= 1000) "${((r.distanceMeters / 100).toInt() / 10.0)} km" else "${r.distanceMeters.toInt()} m"
                                val mins = (r.durationSeconds / 60).toInt()
                                val durationText = if (mins > 0) "$mins min" else "Less than a min"
                                Text("$distText • $durationText", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = org.com.core.ui.theme.AppColorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            }
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                            androidx.compose.material3.Button(
                                onClick = { onEndNavigation() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B))
                            ) {
                                Text("End Navigation", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.White)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
