package org.com.plant.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import org.com.getCurrentEpochMillis
import org.com.plant.components.PlantMapComponent
import org.com.plant.data.PlantRepository
import org.com.plant.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: String,
    repository: PlantRepository = remember { PlantRepository() },
    onBack: () -> Unit
) {
    val trees by repository.trees.collectAsState()
    val tree = trees.find { it.id == plantId } ?: trees.firstOrNull()

    var activeTab by remember { mutableStateOf(0) } // 0: Care & Details, 1: Remote Virtual Inspector
    var showWaterDialog by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var newHeightInput by remember { mutableStateOf("") }
    var careNoteInput by remember { mutableStateOf("") }

    if (tree == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Plant not found", color = Color.White)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tree.name, fontWeight = FontWeight.Black, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            openGoogleMapsNavigation(
                                originLat = -6.7924,
                                originLng = 39.2083,
                                destLat = tree.latitude,
                                destLng = tree.longitude
                            )
                        }
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = "Navigate in Google Maps", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColorScheme.surface)
            )
        },
        containerColor = AppColorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1B5E20),
                                AppColorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TAG: ${tree.tagNumber}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        StatusBadge(status = tree.healthStatus)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(tree.name, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                    Text(tree.species, fontSize = 13.sp, color = Color.LightGray)
                }
            }

            // Navigation Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = AppColorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Care & History", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Spa, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Remote Virtual Inspector", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Satellite, contentDescription = null) }
                )
            }

            when (activeTab) {
                0 -> {
                    // Care & Details View
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Action Buttons
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("QUICK CARE ACTIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showWaterDialog = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                                        ) {
                                            Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Water", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { showHeightDialog = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                        ) {
                                            Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Height", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                openGoogleMapsNavigation(
                                                    originLat = -6.7924,
                                                    originLng = 39.2083,
                                                    destLat = tree.latitude,
                                                    destLng = tree.longitude
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Navigate", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Plant Metrics Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MetricDetailBox("Planted Height", "${tree.plantedHeightCm} cm", Icons.Default.VerticalAlignBottom)
                                    MetricDetailBox("Current Height", "${tree.currentHeightCm} cm", Icons.Default.Straighten)
                                    MetricDetailBox("Growth +", "+${tree.currentHeightCm - tree.plantedHeightCm} cm", Icons.Default.TrendingUp)
                                }
                            }
                        }

                        // Care Timeline History
                        item {
                            Text("CARE LOGS & HISTORY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        if (tree.careLogs.isEmpty()) {
                            item {
                                Text("No care events recorded yet.", fontSize = 12.sp, color = Color.Gray)
                            }
                        } else {
                            items(tree.careLogs) { log ->
                                CareLogItem(log = log)
                            }
                        }
                    }
                }

                1 -> {
                    // Remote Virtual Inspector Tab ("See without physical investigation")
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B22)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Satellite, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("SATELLITE & SENSOR VIRTUAL INSPECTOR", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Monitor tree health, foliage density, and soil condition without physical site visit.", fontSize = 12.sp, color = Color.LightGray)
                                }
                            }
                        }

                        // Satellite View Map Container
                        item {
                            Text("SATELLITE AERIAL ZOOM (20x)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            ) {
                                PlantMapComponent(
                                    modifier = Modifier.fillMaxSize(),
                                    trees = listOf(tree),
                                    selectedTree = tree,
                                    isSatelliteMode = true,
                                    onNavigateToTree = {
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

                        // Remote IoT Sensor Gauge Dashboard
                        item {
                            Text("REMOTE SENSOR TELEMETRY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TelemetryGaugeCard(
                                    title = "Soil Moisture",
                                    value = "${tree.telemetry.soilMoisturePercent}%",
                                    subtitle = if (tree.telemetry.soilMoisturePercent < 35) "Needs Water!" else "Optimal Moisture",
                                    icon = Icons.Default.WaterDrop,
                                    color = if (tree.telemetry.soilMoisturePercent < 35) Color(0xFFFF9800) else Color(0xFF0288D1),
                                    modifier = Modifier.weight(1f)
                                )

                                TelemetryGaugeCard(
                                    title = "Foliage Density",
                                    value = "${tree.telemetry.foliageHealthScore}/100",
                                    subtitle = "Canopy: ${tree.telemetry.estimatedCanopyAreaM2} m²",
                                    icon = Icons.Default.Park,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // AI Canopy Health Scan
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text("AI CANOPY HEALTH DIAGNOSTIC", fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    DiagnosticRow("Chlorophyll Index", "High (Vibrant Green)", Color(0xFF4CAF50))
                                    DiagnosticRow("Pest & Disease Risk", "Low (0.2%)", Color(0xFF4CAF50))
                                    DiagnosticRow("Thermal Stress", "Normal (26.5°C)", Color(0xFF0288D1))
                                    DiagnosticRow("Sunlight Hours", "${tree.telemetry.sunlightHoursPerDay} hrs/day", Color(0xFFFFC107))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Water Dialog
    if (showWaterDialog) {
        AlertDialog(
            onDismissRequest = { showWaterDialog = false },
            title = { Text("Water Plant") },
            text = {
                Column {
                    Text("Log watering for ${tree.name}:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = careNoteInput,
                        onValueChange = { careNoteInput = it },
                        placeholder = { Text("e.g. Watered 10 liters using watering can") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val log = PlantCareLog(
                            id = "LOG-${getCurrentEpochMillis()}",
                            timestamp = getCurrentEpochMillis(),
                            careType = PlantCareType.WATERING,
                            notes = if (careNoteInput.isNotBlank()) careNoteInput else "Watered plant",
                            performedBy = "Campus Gardener"
                        )
                        repository.addCareLog(tree.id, log)
                        careNoteInput = ""
                        showWaterDialog = false
                    }
                ) {
                    Text("SAVE WATERING")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Height Dialog
    if (showHeightDialog) {
        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = { Text("Update Tree Height") },
            text = {
                Column {
                    Text("Current height: ${tree.currentHeightCm} cm")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newHeightInput,
                        onValueChange = { newHeightInput = it },
                        placeholder = { Text("Enter new height in cm (e.g. 110)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedHeight = newHeightInput.toDoubleOrNull()
                        if (parsedHeight != null) {
                            repository.updateHeight(tree.id, parsedHeight)
                        }
                        newHeightInput = ""
                        showHeightDialog = false
                    }
                ) {
                    Text("UPDATE HEIGHT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHeightDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun MetricDetailBox(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun CareLogItem(log: PlantCareLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (log.careType) {
                            PlantCareType.WATERING -> Color(0xFF0288D1).copy(alpha = 0.2f)
                            PlantCareType.FERTILIZING -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            else -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.careType) {
                        PlantCareType.WATERING -> Icons.Default.WaterDrop
                        PlantCareType.FERTILIZING -> Icons.Default.Eco
                        PlantCareType.PRUNING -> Icons.Default.ContentCut
                        PlantCareType.MEASUREMENT -> Icons.Default.Straighten
                        else -> Icons.Default.Spa
                    },
                    contentDescription = null,
                    tint = when (log.careType) {
                        PlantCareType.WATERING -> Color(0xFF0288D1)
                        PlantCareType.FERTILIZING -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(log.careType.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(log.notes, fontSize = 12.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun TelemetryGaugeCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = color)
            Text(subtitle, fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun DiagnosticRow(label: String, result: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(result, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
    }
}
