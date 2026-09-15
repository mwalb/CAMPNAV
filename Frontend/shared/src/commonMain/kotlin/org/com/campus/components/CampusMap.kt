package org.com.campus.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.com.campus.data.CampusLocation
import org.com.campus.data.University

import org.com.campus.navigation.NavigationState
import org.com.campus.navigation.RoutePoint

@Composable
expect fun CampusMap(
    modifier: Modifier = Modifier,
    university: University,
    locations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit = {},
    initialSelectedLocation: CampusLocation? = null,
    navigationState: NavigationState = NavigationState(),
    onStartNavigation: (CampusLocation, CampusLocation?) -> Unit = { _, _ -> },
    onEndNavigation: () -> Unit = {},
    onLocationUpdate: (RoutePoint) -> Unit = {}
)
