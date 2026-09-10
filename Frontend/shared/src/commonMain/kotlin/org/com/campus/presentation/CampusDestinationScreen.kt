package org.com.campus.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.campus.data.CampusLocation
import org.com.campus.data.CampusRepository
import org.com.campus.data.Category
import org.com.campus.data.University
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampusDestinationScreen(
    university: University,
    onBack: () -> Unit,
    onFindOnMap: (CampusLocation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(emptyList<Category>()) }
    var allLocations by remember { mutableStateOf(emptyList<CampusLocation>()) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var expandedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val repository = remember { CampusRepository() }

    LaunchedEffect(university.id) {
        isLoading = true
        error = null
        try {
            categories = repository.getCategories()
            allLocations = repository.getLocations(university.id)
        } catch (e: Exception) {
            error = "Unable to load campus destinations."
            println("[CAMPNAV] Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val filteredLocations = remember(searchQuery, allLocations) {
        if (searchQuery.isBlank()) emptyList()
        else allLocations.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.buildingCode?.contains(searchQuery, ignoreCase = true) == true 
        }.sortedBy { it.name }
    }

    val alphabet = ('A'..'Z').toList()

    Box(modifier = Modifier.fillMaxSize().background(AppColorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp) // Space for fixed bottom card
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Where are you going?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = university.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColorScheme.primary
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColorScheme.primary)
                }
            } else if (error != null) {
                Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Text(error!!, color = Color.Gray)
                }
            } else {
                // Search Section
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Find a building, service or place on campus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            if (it.isNotBlank()) {
                                selectedLetter = null
                                expandedCategoryId = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search destination...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        singleLine = true
                    )
                    
                    if (searchQuery.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface.copy(alpha = 0.5f))
                        ) {
                            Column {
                                if (filteredLocations.isEmpty()) {
                                    Text("No destinations found", modifier = Modifier.padding(16.dp), color = Color.Gray)
                                }
                                filteredLocations.take(5).forEach { location ->
                                    LocationRow(location, categories) {
                                        selectedLocation = it
                                        searchQuery = "" // Clear search after selection
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Destinations
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("QUICK DESTINATIONS", style = MaterialTheme.typography.labelLarge, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickDestButton("Library", Icons.AutoMirrored.Filled.MenuBook) { 
                            searchQuery = "Library" 
                            selectedLetter = null
                            expandedCategoryId = null
                        }
                        QuickDestButton("Food", Icons.Default.Restaurant) { 
                            searchQuery = "Cafeteria"
                            selectedLetter = null
                            expandedCategoryId = null
                        }
                        QuickDestButton("Health", Icons.Default.MedicalServices) { 
                            searchQuery = "Health"
                            selectedLetter = null
                            expandedCategoryId = null
                        }
                        QuickDestButton("Hostel", Icons.Default.Hotel) { 
                            searchQuery = "Hall"
                            selectedLetter = null
                            expandedCategoryId = null
                        }
                        QuickDestButton("ATM", Icons.Default.Payments) { 
                            searchQuery = "Bank"
                            selectedLetter = null
                            expandedCategoryId = null
                        }
                        QuickDestButton("Religious", Icons.Default.AccountBalance) { 
                            searchQuery = "Chapel"
                            selectedLetter = null
                            expandedCategoryId = null
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Categories Section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("CATEGORIES / SERVICES", style = MaterialTheme.typography.labelLarge, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    categories.forEach { category ->
                        CategoryExpandableRow(
                            category = category,
                            locations = allLocations.filter { it.categoryId == category.id },
                            isExpanded = expandedCategoryId == category.id,
                            onExpandToggle = { 
                                expandedCategoryId = if (expandedCategoryId == category.id) null else category.id
                                if (expandedCategoryId != null) {
                                    searchQuery = ""
                                    selectedLetter = null
                                }
                            },
                            onLocationSelected = { selectedLocation = it }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // A-Z Section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("BUILDINGS & LOCATIONS (A-Z)", style = MaterialTheme.typography.labelLarge, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(alphabet) { letter ->
                            val hasLocations = allLocations.any { it.name.startsWith(letter, ignoreCase = true) }
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = hasLocations) { 
                                        selectedLetter = if (selectedLetter == letter) null else letter
                                        if (selectedLetter != null) {
                                            searchQuery = ""
                                            expandedCategoryId = null
                                        }
                                    },
                                color = when {
                                    selectedLetter == letter -> AppColorScheme.primary
                                    hasLocations -> Color.White.copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                },
                                border = if (hasLocations && selectedLetter != letter) BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = letter.toString(),
                                        color = if (selectedLetter == letter) Color.White else if (hasLocations) Color.White else Color.Gray.copy(alpha = 0.3f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = selectedLetter != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            val letterLocations = allLocations.filter { it.name.startsWith(selectedLetter!!, ignoreCase = true) }.sortedBy { it.name }
                            letterLocations.forEach { location ->
                                LocationRow(location, categories) { selectedLocation = it }
                            }
                        }
                    }
                }
            }
        }

        // Selected Destination Sticky Bottom Card
        AnimatedVisibility(
            visible = selectedLocation != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                tonalElevation = 16.dp,
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("SELECTED DESTINATION", style = MaterialTheme.typography.labelSmall, color = AppColorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = AppColorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedLocation?.name ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            val catName = categories.find { it.id == selectedLocation?.categoryId }?.name ?: "Campus Location"
                            Text(catName + " • " + university.shortName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        IconButton(onClick = { selectedLocation = null }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { selectedLocation?.let { onFindOnMap(it) } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
                    ) {
                        Icon(Icons.Default.Map, null)
                        Spacer(Modifier.width(8.dp))
                        Text("FIND ON MAP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickDestButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CategoryExpandableRow(
    category: Category,
    locations: List<CampusLocation>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onLocationSelected: (CampusLocation) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandToggle)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = AppColorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // In a real app we'd map icon names to actual vectors
                        Icon(Icons.Default.ChevronRight, null, tint = AppColorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(category.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(
                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null,
                tint = Color.Gray
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)) {
                if (locations.isEmpty()) {
                    Text("No locations found in this category.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                locations.forEach { location ->
                    Text(
                        text = "○ ${location.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLocationSelected(location) }
                            .padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
    }
}

@Composable
fun LocationRow(
    location: CampusLocation,
    categories: List<Category>,
    onClick: (CampusLocation) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(location) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Place, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(location.name, color = Color.White, fontWeight = FontWeight.Medium)
            val catName = categories.find { it.id == location.categoryId }?.name ?: "Location"
            Text(catName, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}
