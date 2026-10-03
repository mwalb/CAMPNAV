package org.com.community.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.com.campus.utils.isValidCoordinate
import org.com.core.ui.theme.AppColorScheme

@SuppressLint("MissingPermission")
@Composable
actual fun LocationPickerMap(
    modifier: Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onBack: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val initialPos = if (initialLatitude != null && initialLongitude != null) {
        LatLng(initialLatitude, initialLongitude)
    } else {
        LatLng(-6.7924, 39.2083)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 16f)
    }

    var selectedPos by remember { 
        mutableStateOf(initialPos) 
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
            onMapClick = { latLng ->
                selectedPos = latLng
            }
        ) {
            Marker(
                state = rememberMarkerState(position = selectedPos),
                title = "Selected Location",
                draggable = true
            )
        }

        // Top-Left Back Navigation Control `←`
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onBack() },
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to form",
                    tint = Color(0xFF3C4043),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // GPS Button overlay
        FloatingActionButton(
            onClick = {
                locationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                        val currentLatLng = LatLng(loc.latitude, loc.longitude)
                        selectedPos = currentLatLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 17f)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 140.dp, end = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Get GPS Location", tint = Color.White)
        }

        // Bottom Confirmation Card
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = AppColorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "TAP ANYWHERE ON MAP TO PICK LOCATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        "Lat: ${((selectedPos.latitude * 1000000.0).toLong() / 1000000.0)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Lng: ${((selectedPos.longitude * 1000000.0).toLong() / 1000000.0)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Button(
                    onClick = {
                        onConfirm(selectedPos.latitude, selectedPos.longitude)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
                ) {
                    Text("Confirm Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
