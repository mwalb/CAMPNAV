package org.com.community.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.com.campus.data.University
import org.com.community.model.IssueCategory
import org.com.community.model.IssueReport

@Composable
actual fun IssuesMap(
    modifier: Modifier,
    university: University?,
    issues: List<IssueReport>,
    selectedIssue: IssueReport?,
    onIssueSelected: (IssueReport?) -> Unit,
    onBack: () -> Unit
) {
    val initialLat = university?.latitude ?: -6.780543
    val initialLng = university?.longitude ?: 39.204593
    val initialZoom = university?.defaultZoom ?: 15f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), initialZoom)
    }

    LaunchedEffect(university) {
        if (university?.latitude != null && university.longitude != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(university.latitude, university.longitude),
                university.defaultZoom ?: 15f
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true),
            onMapClick = {
                onIssueSelected(null)
            }
        ) {
            issues.forEach { issue ->
                val lat = issue.issueLocation.latitude
                val lng = issue.issueLocation.longitude
                if (lat != 0.0 || lng != 0.0) {
                    val markerState = rememberMarkerState(key = issue.id, position = LatLng(lat, lng))
                    
                    val hue = when (issue.category) {
                        IssueCategory.WATER -> BitmapDescriptorFactory.HUE_CYAN
                        IssueCategory.ELECTRICITY -> BitmapDescriptorFactory.HUE_YELLOW
                        IssueCategory.SAFETY -> BitmapDescriptorFactory.HUE_RED
                        IssueCategory.ROADS -> BitmapDescriptorFactory.HUE_ORANGE
                        else -> BitmapDescriptorFactory.HUE_MAGENTA
                    }

                    Marker(
                        state = markerState,
                        title = issue.category.displayName,
                        snippet = "Status: ${issue.status.name} | Ref: ${issue.id}",
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            onIssueSelected(issue)
                            false
                        }
                    )
                }
            }
        }
    }
}
