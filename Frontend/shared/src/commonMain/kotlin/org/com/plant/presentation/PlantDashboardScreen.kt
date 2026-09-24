package org.com.plant.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.campus.utils.openGoogleMapsNavigation
import org.com.core.ui.theme.AppColorScheme
import org.com.plant.components.PlantMapComponent
import org.com.plant.data.PlantRepository
import org.com.plant.model.PlantHealthStatus
import org.com.plant.model.PlantedTree

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDashboardScreen(
    repository: PlantRepository = remember { PlantRepository() },
    onBack: () -> Unit,
    onPlantNewTree: () -> Unit,
    onTreeSelected: (String) -> Unit
) {
    val trees by repository.trees.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: List, 1: Map, 2: Telemetry
    var selectedFilter by remember { mutableStateOf<PlantHealthStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var mapSelectedTree by remember { mutableStateOf<PlantedTree?>(null) }
    var isSatelliteMode by remember { mutableStateOf(false) }

    val filteredTrees = remember(trees, selectedFilter, searchQuery) {
        trees.filter { tree ->
            val matchesFilter = selectedFilter == null || tree.healthStatus == selectedFilter
            val matchesQuery = searchQuery.isEmpty() ||
                    tree.name.contains(searchQuery, ignoreCase = true) ||
                    tree.species.contains(searchQuery, ignoreCase = true) ||
                    tree.tagNumber.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val healthyCount = trees.count { it.healthStatus == PlantHealthStatus.HEALTHY }
    val alertCount = trees.count { it.healthStatus == PlantHealthStatus.NEEDS_WATER || it.healthStatus == PlantHealthStatus.CRITICAL }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PLANT & TREE MANAGER", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                        Text("${trees.size} Planted Trees • Green Campus", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (selectedTab == 1) {
                        IconButton(onClick = { isSatelliteMode = !isSatelliteMode }) {
                            Icon(
                                imageVector = if (isSatelliteMode) Icons.Default.Map else Icons.Default.Satellite,
                                contentDescription = "Toggle Satellite",
                                tint = if (isSatelliteMode) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPlantNewTree,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("PLANT TREE", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = AppColorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Overview Metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        label = "Planted",
                        value = "${trees.size}",
                        icon = Icons.Default.Park,
                        color = Color(0xFF4CAF50)
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                    MetricItem(
                        label = "Healthy",
                        value = "$healthyCount",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF8BC34A)
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                    MetricItem(
                        label = "Alerts",
                        value = "$alertCount",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFFF9800)
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                    MetricItem(
                        label = "CO₂ Offset",
                        value = "${trees.size * 22}kg",
                        icon = Icons.Default.Eco,
                        color = Color(0xFF00BCD4)
                    )
                }
            }

            // Tab Navigation Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppColorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Planted Trees", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Map Tracker", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Remote Telemetry", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Satellite, contentDescription = null) }
                )
            }

            // Search & Filter Row (for List tab)
            if (selectedTab == 0) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("Search by plant name, species, or tag...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { selectedFilter = null },
                                label = { Text("All Statuses") }
                            )
                        }
                        items(PlantHealthStatus.entries) { status ->
                            FilterChip(
                                selected = selectedFilter == status,
                                onClick = { selectedFilter = status },
                                label = { Text(status.displayName) }
                            )
                        }
                    }
                }
            }

            // Main Content Area based on Selected Tab
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        // List View
                        if (filteredTrees.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Park, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No trees found", color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("Plant a new tree or adjust your filters", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredTrees) { tree ->
                                    TreeCardItem(
                                        tree = tree,
                                        onClick = { onTreeSelected(tree.id) },
                                        onNavigate = {
                                            openGoogleMapsNavigation(
                                                originLat = -6.7924,
                                                originLng = 39.2083,
                                                destLat = tree.latitude,
                                                destLng = tree.longitude
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Map Tracker
                        PlantMapComponent(
                            modifier = Modifier.fillMaxSize(),
                            trees = trees,
                            selectedTree = mapSelectedTree,
                            onTreeSelected = { mapSelectedTree = it },
                            isSatelliteMode = isSatelliteMode,
                            onNavigateToTree = { tree ->
                                openGoogleMapsNavigation(
                                    originLat = -6.7924,
                                    originLng = 39.2083,
                                    destLat = tree.latitude,
                                    destLng = tree.longitude
                                )
                            }
                        )
                    }

                    2 -> {
                        // Remote Telemetry Summary View
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Satellite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("REMOTE VIRTUAL INSPECTIONS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                            Text("Real-time IoT moisture, thermal, and canopy foliage analysis", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            items(trees) { tree ->
                                TelemetryCardItem(
                                    tree = tree,
                                    onClick = { onTreeSelected(tree.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun TreeCardItem(tree: PlantedTree, onClick: () -> Unit, onNavigate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF2E7D32),
                                Color(0xFF1B5E20)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Park, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tree.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(status = tree.healthStatus)
                }

                Spacer(Modifier.height(4.dp))

                Text(tree.species, fontSize = 12.sp, color = Color.Gray)

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${tree.currentHeightCm} cm", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.width(12.dp))

                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${tree.telemetry.soilMoisturePercent}% moisture", fontSize = 12.sp, color = Color(0xFF0288D1))
                }
            }

            IconButton(
                onClick = onNavigate,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Directions, contentDescription = "Navigate", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun StatusBadge(status: PlantHealthStatus) {
    val (bgColor, textColor) = when (status) {
        PlantHealthStatus.HEALTHY -> Color(0xFF4CAF50).copy(alpha = 0.2f) to Color(0xFF4CAF50)
        PlantHealthStatus.NEEDS_WATER -> Color(0xFF03A9F4).copy(alpha = 0.2f) to Color(0xFF03A9F4)
        PlantHealthStatus.NEEDS_FERTILIZER -> Color(0xFFFF9800).copy(alpha = 0.2f) to Color(0xFFFF9800)
        PlantHealthStatus.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.2f) to Color(0xFFF44336)
        PlantHealthStatus.RECOVERING -> Color(0xFF8BC34A).copy(alpha = 0.2f) to Color(0xFF8BC34A)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TelemetryCardItem(tree: PlantedTree, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Park, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(8.dp))
                    Text(tree.name, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("Tag: ${tree.tagNumber}", fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricBox(label = "Soil Moisture", value = "${tree.telemetry.soilMoisturePercent}%", color = Color(0xFF0288D1))
                TelemetryMetricBox(label = "Temperature", value = "${tree.telemetry.temperatureCelsius}°C", color = Color(0xFFFF9800))
                TelemetryMetricBox(label = "Foliage Score", value = "${tree.telemetry.foliageHealthScore}/100", color = Color(0xFF4CAF50))
                TelemetryMetricBox(label = "Sunlight", value = "${tree.telemetry.sunlightHoursPerDay}h/day", color = Color(0xFFFFC107))
            }
        }
    }
}

@Composable
fun TelemetryMetricBox(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 13.sp, color = color)
        Text(label, fontSize = 9.sp, color = Color.Gray)
    }
}
