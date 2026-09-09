package org.com.campus.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import org.com.campus.data.Category
import org.com.campus.data.University
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapScreen(
    university: University,
    onBack: () -> Unit,
    onNavigateToEntertainment: () -> Unit,
    onNavigateToCreator: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var locations by remember { mutableStateOf(emptyList<CampusLocation>()) }
    var categories by remember { mutableStateOf(emptyList<Category>()) }
    var selectedCategoryIds by remember { mutableStateOf(emptySet<Long>()) }
    var searchQuery by remember { mutableStateOf("") }
    
    val repository = remember { CampusRepository() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(university) {
        try {
            categories = repository.getCategories()
        } catch (e: Exception) {
            // Handle error
        }
    }

    LaunchedEffect(university, selectedCategoryIds, searchQuery) {
        try {
            locations = if (searchQuery.isEmpty()) {
                repository.getLocations(university.id, selectedCategoryIds.toList())
            } else {
                repository.searchLocations(university.id, searchQuery, selectedCategoryIds.toList())
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "CAMPUS NAV",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColorScheme.primary
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.5f))
                
                NavigationDrawerItem(
                    label = { Text("University Map") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                NavigationDrawerItem(
                    label = { Text("Entertainment") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateToEntertainment() 
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Content Creator") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateToCreator() 
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))
                
                NavigationDrawerItem(
                    label = { Text("Switch University") },
                    selected = false,
                    onClick = onBack,
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Map
            CampusMap(
                modifier = Modifier.fillMaxSize(),
                university = university,
                locations = locations,
                onLocationSelected = { selectedLocation = it },
                onBack = onBack
            )

            // UI Elements
            Column(modifier = Modifier.fillMaxSize()) {
                // Top controls
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = { scope.launch { drawerState.open() } },
                            containerColor = AppColorScheme.surface,
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).height(56.dp),
                            placeholder = { Text("Search ${university.shortName ?: ""}...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AppColorScheme.surface.copy(alpha = 0.9f),
                                unfocusedContainerColor = AppColorScheme.surface.copy(alpha = 0.9f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategoryIds.contains(category.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryIds = if (isSelected) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                },
                                label = { Text(category.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = AppColorScheme.surface.copy(alpha = 0.7f),
                                    labelColor = Color.White,
                                    selectedContainerColor = AppColorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                // Bottom controls or details
                selectedLocation?.let {
                    LocationDetailSheet(
                        location = it,
                        modifier = Modifier.padding(16.dp),
                        onDismiss = { selectedLocation = null }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationDetailSheet(
    location: CampusLocation,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(location.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(location.buildingCode ?: "Campus Location", style = MaterialTheme.typography.bodySmall, color = AppColorScheme.primary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text(location.description ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { /* Get Directions */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("GET DIRECTIONS")
            }
        }
    }
}
