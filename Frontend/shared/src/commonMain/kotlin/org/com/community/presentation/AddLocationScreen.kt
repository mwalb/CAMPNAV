package org.com.community.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch
import org.com.campus.data.CampusRepository
import org.com.campus.data.Category
import org.com.campus.data.University
import org.com.community.data.CommunityRepository
import org.com.community.model.ContributionRequest
import org.com.community.model.CommunityContribution
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    onBack: () -> Unit,
    onSubmissionSuccess: (String) -> Unit
) {
    var universities by remember { mutableStateOf(emptyList<University>()) }
    var categories by remember { mutableStateOf(emptyList<Category>()) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    var locationName by remember { mutableStateOf("") }
    var areaType by remember { mutableStateOf("UNIVERSITY_AREA") } // UNIVERSITY_AREA or COMMERCIAL_AREA
    var description by remember { mutableStateOf("") }
    var latitudeStr by remember { mutableStateOf("") }
    var longitudeStr by remember { mutableStateOf("") }
    var contributorName by remember { mutableStateOf("") }
    var contributorContact by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submittedContribution by remember { mutableStateOf<CommunityContribution?>(null) }

    val campusRepo = remember { CampusRepository() }
    val communityRepo = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    var showMapPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            universities = campusRepo.getUniversities()
            if (universities.isNotEmpty()) {
                selectedUniversity = universities.first()
                categories = campusRepo.getCategories(selectedUniversity?.id)
                if (categories.isNotEmpty()) {
                    selectedCategory = categories.first()
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load universities: ${e.message}"
        }
    }

    LaunchedEffect(selectedUniversity?.id) {
        selectedUniversity?.id?.let { uniId ->
            try {
                categories = campusRepo.getCategories(uniId)
                if (categories.isNotEmpty()) {
                    selectedCategory = categories.first()
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        if (submittedContribution != null) {
            // Success State View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text("Contribution Submitted Successfully!", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Reference: ${submittedContribution!!.reference}", style = MaterialTheme.typography.titleMedium, color = AppColorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Status: ${submittedContribution!!.status}", color = Color.Yellow, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Area Type: ${submittedContribution!!.areaType}", color = Color.White)
                        if (submittedContribution!!.areaType == "COMMERCIAL_AREA") {
                            Spacer(Modifier.height(4.dp))
                            Text("Payment Status: ${submittedContribution!!.paymentStatus}", color = Color.Cyan)
                            Text("Commercial submission requires administrator verification & payment processing.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else {
                            Text("University area submissions are reviewed for official mapping.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onSubmissionSuccess(submittedContribution!!.reference) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
                ) {
                    Text("View Status / Back")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Location", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (errorMessage != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF512A2A))) {
                            Text(errorMessage!!, color = Color(0xFFFF8A80), modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                item {
                    Text("Select University", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        universities.forEach { uni ->
                            val isSelected = selectedUniversity?.id == uni.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedUniversity = uni },
                                label = { Text(uni.shortName ?: uni.name) }
                            )
                        }
                    }
                }

                item {
                    Text("Location Type", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { areaType = "UNIVERSITY_AREA" },
                            colors = CardDefaults.cardColors(containerColor = if (areaType == "UNIVERSITY_AREA") AppColorScheme.primary.copy(alpha = 0.3f) else AppColorScheme.surfaceVariant),
                            border = if (areaType == "UNIVERSITY_AREA") BorderStroke(2.dp, AppColorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("University Area", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Free submission", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { areaType = "COMMERCIAL_AREA" },
                            colors = CardDefaults.cardColors(containerColor = if (areaType == "COMMERCIAL_AREA") AppColorScheme.primary.copy(alpha = 0.3f) else AppColorScheme.surfaceVariant),
                            border = if (areaType == "COMMERCIAL_AREA") BorderStroke(2.dp, AppColorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Commercial Area", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Chargeable (Pending Payment)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFB74D))
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Location Name (e.g. Campus Bookshop, NMB)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                item {
                    Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(5).forEach { cat ->
                            val isSelected = selectedCategory?.id == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                item {
                    Text("Location Coordinates (Choose Option)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                // Option B: Current Location Simulation / Preset
                                selectedUniversity?.let {
                                    latitudeStr = (it.latitude ?: -6.7801).toString()
                                    longitudeStr = (it.longitude ?: 39.2041).toString()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Current Loc", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                selectedUniversity?.let {
                                    latitudeStr = (it.latitude?.plus(0.001) ?: -6.7801).toString()
                                    longitudeStr = (it.longitude?.plus(0.001) ?: 39.2041).toString()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pick Map", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latitudeStr,
                            onValueChange = { latitudeStr = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = longitudeStr,
                            onValueChange = { longitudeStr = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }

                item {
                    Text("Contributor Information (Anonymous supported)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contributorName,
                        onValueChange = { contributorName = it },
                        label = { Text("Your Name (Optional / Anonymous)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contributorContact,
                        onValueChange = { contributorContact = it },
                        label = { Text("Contact Email or Phone (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (selectedUniversity == null || locationName.isBlank() || latitudeStr.toDoubleOrNull() == null || longitudeStr.toDoubleOrNull() == null) {
                                errorMessage = "Please fill in university, location name, and valid coordinates."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val req = ContributionRequest(
                                        universityId = selectedUniversity!!.id,
                                        locationName = locationName.trim(),
                                        areaType = areaType,
                                        categoryId = selectedCategory?.id,
                                        description = description.ifBlank { null },
                                        latitude = latitudeStr.toDouble(),
                                        longitude = longitudeStr.toDouble(),
                                        contributorName = contributorName.ifBlank { "Anonymous Contributor" },
                                        contributorContact = contributorContact.ifBlank { "n/a" }
                                    )
                                    val result = communityRepo.submitContribution(req)
                                    submittedContribution = result
                                } catch (e: Exception) {
                                    errorMessage = "Submission failed: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Submit Contribution", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
