package org.com.plant.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.campus.utils.getCurrentUserLocation
import org.com.community.components.CommunityMediaPicker
import org.com.community.components.LocationPickerMap
import org.com.core.ui.theme.AppColorScheme
import org.com.getCurrentEpochMillis
import org.com.plant.data.PlantRepository
import org.com.plant.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantRegistrationScreen(
    repository: PlantRepository = remember { PlantRepository() },
    onBack: () -> Unit,
    onPlantCreated: (String) -> Unit
) {
    var plantName by remember { mutableStateOf("") }
    var plantSpecies by remember { mutableStateOf("Adansonia digitata (Baobab)") }
    var heightCmText by remember { mutableStateOf("50") }
    var notes by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    // Location state - default to UDSM campus / Dar es Salaam
    var latitude by remember { mutableStateOf(-6.7824) }
    var longitude by remember { mutableStateOf(39.2083) }
    var addressText by remember { mutableStateOf("University Campus Green Area") }
    var isLocating by remember { mutableStateOf(false) }

    val popularSpecies = listOf(
        "Adansonia digitata (Baobab)",
        "Azadirachta indica (Neem)",
        "Khaya senegalensis (Mahogany)",
        "Acacia tortilis (Umbrella Thorn)",
        "Mangifera indica (Mango)",
        "Jacaranda mimosifolia (Jacaranda)",
        "Persea americana (Avocado)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REGISTER PLANTED TREE", fontWeight = FontWeight.Black, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: Tree Details
            Text("TREE DETAILS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = plantName,
                onValueChange = { plantName = it },
                label = { Text("Plant Name / Designation") },
                placeholder = { Text("e.g. Science Quad Baobab #1") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Park, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )

            Spacer(Modifier.height(12.dp))

            Text("Select Species:", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(popularSpecies) { species ->
                    FilterChip(
                        selected = plantSpecies == species,
                        onClick = { plantSpecies = species },
                        label = { Text(species.take(22) + if (species.length > 22) "..." else "") }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = heightCmText,
                onValueChange = { heightCmText = it },
                label = { Text("Initial Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )

            Spacer(Modifier.height(20.dp))

            // Section 2: Planting Location (Map Picker & Current GPS Location)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SET PLANTING LOCATION", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, letterSpacing = 1.sp)

                TextButton(
                    onClick = {
                        isLocating = true
                        getCurrentUserLocation { lat, lng ->
                            latitude = lat
                            longitude = lng
                            isLocating = false
                        }
                    }
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("USE CURRENT LOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text("Tap on map or drag marker to set tree coordinates:", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)
            ) {
                LocationPickerMap(
                    modifier = Modifier.fillMaxSize(),
                    initialLatitude = latitude,
                    initialLongitude = longitude,
                    onLocationSelected = { lat, lng ->
                        latitude = lat
                        longitude = lng
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPS: ${latitude.toString().take(8)}, ${longitude.toString().take(8)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        isLocating = true
                        getCurrentUserLocation { lat, lng ->
                            latitude = lat
                            longitude = lng
                            isLocating = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Recenter GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 3: Photo & Notes
            Text("PHOTOS & CARE NOTES", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))

            CommunityMediaPicker(
                onMediaCaptured = { uri -> selectedPhotoUri = uri }
            ) { triggerCamera, triggerGallery ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = triggerCamera,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("TAKE PHOTO")
                    }

                    OutlinedButton(
                        onClick = triggerGallery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("GALLERY")
                    }
                }
            }

            if (selectedPhotoUri != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Photo attached", fontSize = 12.sp, color = Color(0xFF4CAF50))
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Planting Notes & Environmental Condition") },
                placeholder = { Text("e.g., Soil prepared with organic compost, near water tap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(Modifier.height(28.dp))

            // Save Action Button
            Button(
                onClick = {
                    val parsedHeight = heightCmText.toDoubleOrNull() ?: 50.0
                    val newTree = PlantedTree(
                        id = "PLANT-${getCurrentEpochMillis()}",
                        tagNumber = "TREE-2025-${(100..999).random()}",
                        name = if (plantName.isNotBlank()) plantName else plantSpecies.split(" ").first() + " Tree",
                        species = plantSpecies,
                        plantedDate = getCurrentEpochMillis(),
                        latitude = latitude,
                        longitude = longitude,
                        address = addressText,
                        healthStatus = PlantHealthStatus.HEALTHY,
                        plantedHeightCm = parsedHeight,
                        currentHeightCm = parsedHeight,
                        coverPhotoUri = selectedPhotoUri ?: "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d",
                        photoHistory = listOfNotNull(selectedPhotoUri ?: "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d"),
                        telemetry = PlantSensorTelemetry(
                            soilMoisturePercent = 85,
                            temperatureCelsius = 26.0,
                            sunlightHoursPerDay = 8.0,
                            foliageHealthScore = 95,
                            estimatedCanopyAreaM2 = 0.5,
                            lastSensorSync = getCurrentEpochMillis()
                        ),
                        notes = notes,
                        plantedBy = "Campus Tree Caretaker"
                    )

                    repository.addTree(newTree)
                    onPlantCreated(newTree.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Park, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SAVE & PLANT TREE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
