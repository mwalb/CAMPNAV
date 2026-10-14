package org.com.community.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
    val fallbackLat = university?.latitude ?: -6.780543
    val fallbackLng = university?.longitude ?: 39.204593
    val fallbackZoom = university?.defaultZoom ?: 15f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(fallbackLat, fallbackLng), fallbackZoom)
    }

    // Filter valid issue coordinates (exclude missing or 0.0/0.0 coordinates)
    val validIssues = remember(issues) {
        issues.filter { it.issueLocation.latitude != 0.0 || it.issueLocation.longitude != 0.0 }
    }

    val issuesKey = remember(validIssues) {
        validIssues.joinToString(";") { "${it.id}_${it.issueLocation.latitude}_${it.issueLocation.longitude}" }
    }

    // Update camera position when selected university or issue dataset changes
    LaunchedEffect(university?.id, issuesKey) {
        val univLat = university?.latitude ?: fallbackLat
        val univLng = university?.longitude ?: fallbackLng

        if (validIssues.isEmpty()) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(univLat, univLng), university?.defaultZoom ?: 15f)
            )
        } else {
            try {
                val boundsBuilder = LatLngBounds.Builder()
                boundsBuilder.include(LatLng(univLat, univLng))

                validIssues.forEach { issue ->
                    boundsBuilder.include(
                        LatLng(issue.issueLocation.latitude, issue.issueLocation.longitude)
                    )
                }

                val bounds = boundsBuilder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 120)
                )
            } catch (e: Exception) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(univLat, univLng), university?.defaultZoom ?: 15f)
                )
            }
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
            validIssues.forEach { issue ->
                val lat = issue.issueLocation.latitude
                val lng = issue.issueLocation.longitude
                val markerKey = "${issue.id}_${issue.status.name}"
                val markerState = rememberMarkerState(key = markerKey, position = LatLng(lat, lng))

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
                    snippet = "Status: ${issue.status.name} | Ref: ${issue.id.ifBlank { "CAM-REF" }}",
                    icon = BitmapDescriptorFactory.defaultMarker(hue),
                    onClick = {
                        onIssueSelected(issue)
                        false
                    }
                )
            }
        }

        // Back Navigation Button inside Issues Map
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
