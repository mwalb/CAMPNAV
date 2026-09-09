package org.com.campus.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.com.campus.components.CampusMap
import org.com.campus.data.CampusLocation
import org.com.campus.data.CampusRepository
import org.com.campus.data.University

@Composable
fun CampusMapScreen(
    university: University,
    onBack: () -> Unit
) {
    var locations by remember { mutableStateOf(emptyList<CampusLocation>()) }
    val repository = remember { CampusRepository() }

    LaunchedEffect(university.id) {
        println("[CAMPNAV] Loading locations for university: ${university.name} (ID: ${university.id})")
        try {
            val fetchedLocations = repository.getLocations(university.id)
            
            // Defensive filtering: Ensure locations belong to selected university and are active
            val validLocations = fetchedLocations.filter { 
                it.isActive && (it.universityId == null || it.universityId == university.id)
            }
            
            locations = validLocations
            println("[CAMPNAV] API returned ${fetchedLocations.size} locations. Valid for map: ${locations.size}")
        } catch (e: Exception) {
            println("[CAMPNAV] Error fetching locations: ${e.message}")
        }
    }

    // Strictly map-only screen as per requirement
    CampusMap(
        modifier = Modifier.fillMaxSize(),
        university = university,
        locations = locations,
        onLocationSelected = { location ->
            println("[CAMPNAV] Marker clicked: ${location.name} (ID: ${location.id})")
        },
        onBack = onBack
    )
}
