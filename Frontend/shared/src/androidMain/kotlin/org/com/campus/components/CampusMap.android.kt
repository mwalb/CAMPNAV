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
                onMapClick(RoutePoint(latLng.latitude, latLng.longitude))
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
            when (navigationState.status) {
                NavigationStatus.SHOWING_NAV_CHOICE -> {
                    AlertDialog(
                        onDismissRequest = { onEndNavigation() },
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
                                        locationClient.lastLocation.addOnSuccessListener { loc ->
                                            if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                                                openGoogleMapsNavigation(loc.latitude, loc.longitude, navigationState.destination.latitude, navigationState.destination.longitude)
                                                onEndNavigation()
                                            } else {
                                                // Fallback to fresh location if lastLocation is null
                                                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build()
                                                locationClient.requestLocationUpdates(request, object : LocationCallback() {
                                                    override fun onLocationResult(result: LocationResult) {
                                                        result.lastLocation?.let { freshLoc ->
                                                            if (isValidCoordinate(freshLoc.latitude, freshLoc.longitude)) {
                                                                openGoogleMapsNavigation(freshLoc.latitude, freshLoc.longitude, navigationState.destination.latitude, navigationState.destination.longitude)
                                                                onEndNavigation()
                                                            }
                                                        }
                                                    }
                                                }, null)
                                            }
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
                                    onClick = { onStatusChange(NavigationStatus.SELECTING_START_POINT) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = org.com.core.ui.theme.AppColorScheme.primary)
                                ) {
                                    Icon(Icons.Default.PinDrop, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Select starting point")
                                }
                                TextButton(onClick = { onEndNavigation() }) {
                                    Text("Cancel", color = Color.Red)
                                }
                            }
                        }
                    )
                }

                NavigationStatus.SELECTING_START_POINT -> {
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredLocations = remember(searchQuery, locations) {
                        if (searchQuery.isBlank()) emptyList()
                        else locations.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Select a starting point",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            
                            androidx.compose.material3.OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search location...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            
                            if (filteredLocations.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    filteredLocations.take(3).forEach { loc ->
                                        androidx.compose.material3.TextButton(
                                            onClick = {
                                                onMapClick(RoutePoint(loc.latitude, loc.longitude))
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(loc.name, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    locationClient.lastLocation.addOnSuccessListener { loc ->
                                        if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                                            onMapClick(RoutePoint(loc.latitude, loc.longitude))
                                        } else {
                                            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build()
                                            locationClient.requestLocationUpdates(request, object : LocationCallback() {
                                                override fun onLocationResult(result: LocationResult) {
                                                    result.lastLocation?.let { freshLoc ->
                                                        if (isValidCoordinate(freshLoc.latitude, freshLoc.longitude)) {
                                                            onMapClick(RoutePoint(freshLoc.latitude, freshLoc.longitude))
                                                        }
                                                    }
                                                }
                                            }, null)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = org.com.core.ui.theme.AppColorScheme.primary)
                            ) {
                                Icon(Icons.Default.MyLocation, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Use Current Location")
                            }
                            
                            Text(
                                "Or tap on the map",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    
                    ExtendedFloatingActionButton(
                        onClick = { onStatusChange(NavigationStatus.SHOWING_NAV_CHOICE) },
                        icon = { Icon(Icons.Default.ArrowBack, null) },
                        text = { Text("Back") },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    )
                }

                NavigationStatus.CONFIRMING_START_POINT -> {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Starting point selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    navigationState.selectedStartPoint?.let { start ->
                                        openGoogleMapsNavigation(start.latitude, start.longitude, navigationState.destination.latitude, navigationState.destination.longitude)
                                        onEndNavigation()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Use this starting point")
                            }
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = { onStatusChange(NavigationStatus.SELECTING_START_POINT) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Choose again")
                            }
                            TextButton(onClick = { onEndNavigation() }) {
                                Text("Cancel", color = Color.Red)
                            }
                        }
                    }
                }

                NavigationStatus.SELECTING_ORIGIN -> {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
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
                    }
                }
                NavigationStatus.CALCULATING -> {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Calculating route to ${navigationState.destination.name}...", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = org.com.core.ui.theme.AppColorScheme.primary
                            )
                        }
                    }
                }
                NavigationStatus.ACTIVE -> {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                    }
                }
                else -> {}
            }
        }
    }
}
