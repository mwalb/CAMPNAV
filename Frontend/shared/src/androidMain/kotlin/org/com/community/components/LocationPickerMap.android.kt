package org.com.community.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.com.campus.utils.isValidCoordinate

@SuppressLint("MissingPermission")
@Composable
actual fun LocationPickerMap(
    modifier: Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val initialPos = if (initialLatitude != null && initialLongitude != null) {
        LatLng(initialLatitude, initialLongitude)
    } else {
        // Default to a central location (e.g., Dar es Salaam) if no initial pos
        LatLng(-6.7924, 39.2083)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 15f)
    }

    var markerPosition by remember { 
        mutableStateOf(if (initialLatitude != null && initialLongitude != null) LatLng(initialLatitude, initialLongitude) else null) 
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
            onMapClick = { latLng ->
                markerPosition = latLng
                onLocationSelected(latLng.latitude, latLng.longitude)
            }
        ) {
            markerPosition?.let { pos ->
                Marker(
                    state = rememberMarkerState(position = pos),
                    title = "Selected Location",
                    draggable = true
                )
            }
        }

        // GPS Button overlay
        FloatingActionButton(
            onClick = {
                locationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                        val currentLatLng = LatLng(loc.latitude, loc.longitude)
                        markerPosition = currentLatLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 17f)
                        onLocationSelected(loc.latitude, loc.longitude)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Get GPS Location", tint = Color.White)
        }
    }
}
