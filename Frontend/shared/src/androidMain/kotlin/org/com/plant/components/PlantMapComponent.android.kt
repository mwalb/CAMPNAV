package org.com.plant.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Park
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.com.campus.utils.isValidCoordinate
import org.com.plant.model.PlantHealthStatus
import org.com.plant.model.PlantedTree

@SuppressLint("MissingPermission")
@Composable
actual fun PlantMapComponent(
    modifier: Modifier,
    trees: List<PlantedTree>,
    selectedTree: PlantedTree?,
    onTreeSelected: (PlantedTree) -> Unit,
    isSatelliteMode: Boolean,
    onNavigateToTree: (PlantedTree) -> Unit
) {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val defaultPosition = remember(selectedTree, trees) {
        when {
            selectedTree != null -> LatLng(selectedTree.latitude, selectedTree.longitude)
            trees.isNotEmpty() -> LatLng(trees.first().latitude, trees.first().longitude)
            else -> LatLng(-6.7924, 39.2083) // Default Tanzania/UDSM
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, if (isSatelliteMode) 19f else 15f)
    }

    LaunchedEffect(selectedTree, isSatelliteMode) {
        if (selectedTree != null) {
            val target = LatLng(selectedTree.latitude, selectedTree.longitude)
            val zoomLevel = if (isSatelliteMode) 19.5f else 16.5f
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(target, zoomLevel)
                ),
                1000
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapType = if (isSatelliteMode) MapType.SATELLITE else MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            ),
            onMapClick = { }
        ) {
            trees.forEach { tree ->
                val isSelected = selectedTree?.id == tree.id
                val markerState = rememberMarkerState(
                    key = tree.id,
                    position = LatLng(tree.latitude, tree.longitude)
                )

                val markerHue = when (tree.healthStatus) {
                    PlantHealthStatus.HEALTHY -> BitmapDescriptorFactory.HUE_GREEN
                    PlantHealthStatus.NEEDS_WATER -> BitmapDescriptorFactory.HUE_CYAN
                    PlantHealthStatus.NEEDS_FERTILIZER -> BitmapDescriptorFactory.HUE_ORANGE
                    PlantHealthStatus.CRITICAL -> BitmapDescriptorFactory.HUE_RED
                    PlantHealthStatus.RECOVERING -> BitmapDescriptorFactory.HUE_YELLOW
                }

                Marker(
                    state = markerState,
                    title = tree.name,
                    snippet = "${tree.species} • ${tree.healthStatus.displayName}",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        onTreeSelected(tree)
                        false
                    }
                )
            }
        }

        // GPS Re-center button
        FloatingActionButton(
            onClick = {
                locationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(loc.latitude, loc.longitude), 16f
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Current GPS Location", tint = MaterialTheme.colorScheme.primary)
        }

        // Selected Tree Quick Info Card (Overlay)
        selectedTree?.let { tree ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (tree.healthStatus) {
                                    PlantHealthStatus.HEALTHY -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    PlantHealthStatus.NEEDS_WATER -> Color(0xFF03A9F4).copy(alpha = 0.2f)
                                    else -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Park,
                            contentDescription = null,
                            tint = when (tree.healthStatus) {
                                PlantHealthStatus.HEALTHY -> Color(0xFF2E7D32)
                                PlantHealthStatus.NEEDS_WATER -> Color(0xFF0288D1)
                                else -> Color(0xFFE65100)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tree.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tree.species,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Tag: ${tree.tagNumber} • Height: ${tree.currentHeightCm}cm",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onNavigateToTree(tree) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("NAVIGATE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
