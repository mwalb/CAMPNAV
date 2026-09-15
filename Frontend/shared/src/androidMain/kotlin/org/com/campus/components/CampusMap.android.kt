package org.com.campus.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
                Marker(
                    state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                    title = location.name,
                    snippet = location.description,
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
    }
}
