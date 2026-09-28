package org.com.campus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.com.campus.components.CampusMap
import org.com.campus.data.CampusLocation
import org.com.campus.data.CampusRepository
import org.com.campus.data.University
import org.com.campus.navigation.*
import org.com.campus.utils.openGoogleMapsNavigation

@Composable
fun CampusMapScreen(
    university: University,
    initialSelectedLocation: CampusLocation? = null,
    selectedCategoryId: Long? = null,
    onBack: () -> Unit
) {
    var locations by remember { mutableStateOf(emptyList<CampusLocation>()) }
    var navigationState by remember { mutableStateOf(NavigationState()) }
    var activeSelectedLocation by remember { mutableStateOf(initialSelectedLocation) }
    var searchQuery by remember { mutableStateOf("") }

    val repository = remember { CampusRepository() }
    val routingService = remember { GoogleRoutingService("AIzaSyAvna2gdB_NvFXsBGgY-WBnatqy9IKUN5s") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(university.id) {
        println("[CAMPNAV] UNIVERSITY SELECTED: ${university.name} (ID: ${university.id})")
        searchQuery = ""
        activeSelectedLocation = null
        try {
            val fetchedLocations = repository.getLocations(university.id)
            locations = fetchedLocations.filter { it.isActive }
            println("[CAMPNAV] LOADED ${locations.size} locations for ${university.name}")
        } catch (e: Exception) {
            println("[CAMPNAV] Error: ${e.message}")
        }
    }

    // Handle initial state and navigation flag (1001L)
    LaunchedEffect(initialSelectedLocation, selectedCategoryId) {
        if (initialSelectedLocation != null) {
            activeSelectedLocation = initialSelectedLocation
            if (selectedCategoryId == 1001L) {
                navigationState = navigationState.copy(
                    destination = initialSelectedLocation,
                    status = NavigationStatus.SELECTING_ORIGIN
                )
            } else {
                navigationState = navigationState.copy(
                    destination = initialSelectedLocation,
                    status = NavigationStatus.IDLE
                )
            }
        }
    }

    val searchSuggestions = remember(searchQuery, locations) {
        val q = searchQuery.trim()
        if (q.isBlank()) emptyList()
        else {
            val results = locations.filter {
                it.name.contains(q, ignoreCase = true) ||
                        it.officialName?.contains(q, ignoreCase = true) == true ||
                        it.buildingCode?.contains(q, ignoreCase = true) == true ||
                        it.aliases?.split(";")?.any { alias -> alias.trim().contains(q, ignoreCase = true) } == true
            }.sortedBy { it.name }.take(8)
            println("[CAMPNAV] SEARCH QUERY: '$q', SEARCH SUGGESTIONS count: ${results.size} for university ${university.name}")
            results
        }
    }

    // Filter locations: display only selected content (Content View) or category locations (Category View) or all locations
    val displayLocations = remember(locations, activeSelectedLocation, selectedCategoryId) {
        when {
            activeSelectedLocation != null -> {
                println("[CAMPNAV] CONTENT VIEW: displaying only selected location ${activeSelectedLocation!!.name} (${activeSelectedLocation!!.latitude}, ${activeSelectedLocation!!.longitude})")
                listOf(activeSelectedLocation!!)
            }
            selectedCategoryId != null -> {
                println("[CAMPNAV] CATEGORY VIEW: displaying category locations for id $selectedCategoryId")
                locations.filter { it.categoryId == selectedCategoryId }
            }
            else -> {
                println("[CAMPNAV] ALL LOCATIONS VIEW: displaying all active locations")
                locations
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CampusMap(
            modifier = Modifier.fillMaxSize(),
            university = university,
            locations = displayLocations,
            allLocations = locations,
            onLocationSelected = { location ->
                println("[CAMPNAV] LOCATION SELECTED ON MAP: ${location.name}")
                activeSelectedLocation = location
                navigationState = navigationState.copy(
                    destination = location,
                    status = NavigationStatus.IDLE
                )
            },
            onBack = onBack,
            initialSelectedLocation = activeSelectedLocation,
            navigationState = navigationState,
            onStartNavigation = { dest, _ ->
                println("[CAMPNAV] NAVIGATION START: destination=${dest.name}")
                navigationState = navigationState.copy(
                    destination = dest,
                    status = NavigationStatus.SHOWING_NAV_CHOICE
                )
            },
            onEndNavigation = {
                if (navigationState.status != NavigationStatus.IDLE) {
                    println("[CAMPNAV] NAVIGATION SESSION RESET / RETURNED TO IDLE")
                    navigationState = NavigationState()
                }
            },
            onLocationUpdate = { point ->
                val prevLoc = navigationState.userLocation
                navigationState = navigationState.copy(userLocation = point)
            },
            onMapClick = { point ->
                if (navigationState.status == NavigationStatus.SELECTING_START_POINT) {
                    navigationState.destination?.let { dest ->
                        println("[CAMPNAV] GOOGLE MAPS HANDOFF (Manual Start): origin=${point.latitude},${point.longitude}, destination=${dest.latitude},${dest.longitude}")
                        openGoogleMapsNavigation(point.latitude, point.longitude, dest.latitude, dest.longitude)
                    }
                    navigationState = NavigationState()
                }
            },
            onStatusChange = { status ->
                println("[CAMPNAV] NAVIGATION STATUS CHANGED: $status")
                navigationState = navigationState.copy(status = status)
            }
        )

        // Map Search Bar & Suggestions Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 72.dp, end = 16.dp)
                .fillMaxWidth(0.9f)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search ${university.shortName ?: "campus"} locations...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                } else null,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xEE0F0F23),
                    unfocusedContainerColor = Color(0xEE0F0F23),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            if (searchSuggestions.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A35)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp).padding(8.dp)) {
                        items(searchSuggestions) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        println("[CAMPNAV] CONTENT SELECTED: ${suggestion.name}")
                                        println("[CAMPNAV] DESTINATION SELECTED: ${suggestion.name}")
                                        activeSelectedLocation = suggestion
                                        navigationState = navigationState.copy(
                                            destination = suggestion,
                                            status = NavigationStatus.IDLE
                                        )
                                        searchQuery = ""
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(suggestion.name, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3
    val phi1 = lat1 * kotlin.math.PI / 180
    val phi2 = lat2 * kotlin.math.PI / 180
    val dphi = (lat2 - lat1) * kotlin.math.PI / 180
    val dlambda = (lon2 - lon1) * kotlin.math.PI / 180
    val a = kotlin.math.sin(dphi / 2) * kotlin.math.sin(dphi / 2) +
            kotlin.math.cos(phi1) * kotlin.math.cos(phi2) *
            kotlin.math.sin(dlambda / 2) * kotlin.math.sin(dlambda / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return r * c
}
