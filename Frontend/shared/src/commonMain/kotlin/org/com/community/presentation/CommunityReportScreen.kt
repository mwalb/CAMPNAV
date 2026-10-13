package org.com.community.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.com.campus.utils.getCurrentUserLocation
import org.com.community.components.CommunityMediaPicker
import org.com.community.components.LocationPickerMap
import org.com.community.model.IssueCategory
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityReportScreen(
    onBack: () -> Unit
) {
    val viewModel = remember { CommunityReportViewModel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColorScheme.background, Color(0xFF13132B))
                )
            )
    ) {
        if (viewModel.showMapPicker) {
            Box(modifier = Modifier.fillMaxSize()) {
                LocationPickerMap(
                    modifier = Modifier.fillMaxSize(),
                    initialLatitude = viewModel.issueLocation?.latitude,
                    initialLongitude = viewModel.issueLocation?.longitude,
                    onBack = { viewModel.showMapPicker = false },
                    onConfirm = { lat, lng ->
                        viewModel.onLocationSelected(lat, lng, "PICK_FROM_MAP")
                    }
                )
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Report a Campus Issue", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (viewModel.isSubmitted) {
                        QuickReportSuccessView(
                            viewModel = viewModel,
                            onDone = onBack
                        )
                    } else {
                        QuickReportFormView(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickReportFormView(viewModel: CommunityReportViewModel) {
    val universities = listOf(
        Pair(1L, "UDSM (Dar es Salaam)"),
        Pair(2L, "UDOM (Dodoma)"),
        Pair(3L, "MUST (Mbeya)"),
        Pair(4L, "SUA (Morogoro)")
    )

    val quickCategories = listOf(
        QuickCategoryItem(IssueCategory.WATER, "Water", Icons.Default.WaterDrop),
        QuickCategoryItem(IssueCategory.ELECTRICITY, "Electricity", Icons.Default.ElectricBolt),
        QuickCategoryItem(IssueCategory.ROADS, "Roads", Icons.Default.AddRoad),
        QuickCategoryItem(IssueCategory.SANITATION, "Sanitation", Icons.Default.CleaningServices),
        QuickCategoryItem(IssueCategory.SAFETY, "Security", Icons.Default.Security),
        QuickCategoryItem(IssueCategory.BUILDINGS, "Buildings", Icons.Default.Domain),
        QuickCategoryItem(IssueCategory.INTERNET, "Internet", Icons.Default.Wifi),
        QuickCategoryItem(IssueCategory.WASTE, "Waste", Icons.Default.Delete),
        QuickCategoryItem(IssueCategory.OTHER, "Other", Icons.Default.MoreHoriz)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        // Scope Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CAMPUS SCOPE",
                fontSize = 11.sp,
                color = AppColorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            universities.forEach { (id, name) ->
                val isSelected = viewModel.selectedUniversityId == id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.selectedUniversityId = id
                        viewModel.selectedUniversityName = name
                    },
                    label = {
                        Text(
                            name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColorScheme.primary,
                        containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 1. WHAT IS WRONG?
        Text(
            text = "1. WHAT IS WRONG?",
            fontSize = 12.sp,
            color = AppColorScheme.primary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            items(quickCategories) { item ->
                val isSelected = viewModel.selectedCategory == item.category
                QuickCategoryTile(
                    label = item.label,
                    icon = item.icon,
                    isSelected = isSelected,
                    onClick = { viewModel.selectedCategory = item.category }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 2. WHERE IS IT?
        Text(
            text = "2. WHERE IS IT?",
            fontSize = 12.sp,
            color = AppColorScheme.primary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AppColorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = AppColorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.issueLocation != null)
                                "Lat: ${((viewModel.issueLocation!!.latitude * 10000).toLong() / 10000.0)}, Lng: ${((viewModel.issueLocation!!.longitude * 10000).toLong() / 10000.0)}"
                            else "No location selected",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (viewModel.issueLocation != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Confirmed",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.isFetchingCurrentLocation = true
                            getCurrentUserLocation { lat, lng ->
                                viewModel.onLocationSelected(lat, lng, "CURRENT_LOCATION")
                                viewModel.isFetchingCurrentLocation = false
                            }
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (viewModel.isFetchingCurrentLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Use Current Location", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Button(
                        onClick = { viewModel.showMapPicker = true },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick on Map", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 3. DESCRIBE THE PROBLEM
        Text(
            text = "3. DESCRIBE THE PROBLEM",
            fontSize = 12.sp,
            color = AppColorScheme.primary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Water is leaking outside the library", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AppColorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp),
            minLines = 2,
            maxLines = 4
        )

        if (viewModel.derivedTitle.isNotBlank()) {
            Text(
                text = "Suggested Title: ${viewModel.derivedTitle}",
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // 4. URGENCY
        Text(
            text = "4. URGENCY",
            fontSize = 12.sp,
            color = AppColorScheme.primary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("LOW", "NORMAL", "HIGH", "EMERGENCY").forEach { level ->
                val isSelected = viewModel.urgency == level
                val activeColor = when (level) {
                    "EMERGENCY" -> Color(0xFFDC2626)
                    "HIGH" -> Color(0xFFEA580C)
                    "NORMAL" -> AppColorScheme.primary
                    else -> Color(0xFF4B5563)
                }

                Surface(
                    onClick = { viewModel.urgency = level },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) activeColor else activeColor.copy(alpha = 0.15f),
                    border = if (isSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, activeColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(level, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (viewModel.urgency == "EMERGENCY") {
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDC2626).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "CAMPNAV is an issue tracking tool. For active danger, fire, or crimes in progress, contact campus security directly.",
                        fontSize = 10.sp,
                        color = Color.White,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 5. ADD DETAILS (OPTIONAL) ACCORDION (PLACED ABOVE SUBMIT BUTTON)
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = AppColorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showOptionalDetails = !viewModel.showOptionalDetails },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add details (optional)",
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Icon(
                        if (viewModel.showOptionalDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle optional details",
                        tint = Color.LightGray
                    )
                }

                if (viewModel.showOptionalDetails) {
                    Spacer(Modifier.height(16.dp))

                    Text("ATTACH PHOTOS", fontSize = 11.sp, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    CommunityMediaPicker(
                        onMediaCaptured = { viewModel.addEvidence(it) }
                    ) { captureImage, pickGallery ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = captureImage,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Camera", color = Color.White, fontSize = 12.sp)
                            }
                            Button(
                                onClick = pickGallery,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Gallery", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    if (viewModel.evidenceUris.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewModel.evidenceUris) { uri ->
                                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(10.dp))) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeEvidence(uri) },
                                        modifier = Modifier.align(Alignment.TopEnd).size(22.dp).background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("PRECISE LOCATION / BUILDING NOTE", fontSize = 11.sp, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = viewModel.detailedLocationNote,
                        onValueChange = { viewModel.detailedLocationNote = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        placeholder = { Text("e.g. 2nd floor near Room 204", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("REPORTER INFORMATION", fontSize = 11.sp, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = viewModel.reporterName,
                        onValueChange = { viewModel.reporterName = it },
                        label = { Text("Your name *", color = Color.LightGray) },
                        placeholder = { Text("Enter your full name", color = Color.Gray) },
                        singleLine = true,
                        isError = viewModel.reporterName.isNotEmpty() && viewModel.reporterName.trim().isEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppColorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            errorBorderColor = Color(0xFFEF4444)
                        )
                    )

                    OutlinedTextField(
                        value = viewModel.reporterEmail,
                        onValueChange = { viewModel.reporterEmail = it },
                        label = { Text("Email (optional)", color = Color.LightGray) },
                        placeholder = { Text("Enter email for status updates (optional)", color = Color.Gray) },
                        singleLine = true,
                        isError = !viewModel.isEmailValid,
                        supportingText = {
                            if (!viewModel.isEmailValid) {
                                Text("Invalid email format (or leave blank)", color = Color(0xFFEF4444), fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppColorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            errorBorderColor = Color(0xFFEF4444)
                        )
                    )
                }
            }
        }

        // Error message & Retry banner if submission failed
        viewModel.submissionError?.let { err ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFFD32F2F))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(err, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.retrySubmit() }) {
                        Text("RETRY", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 6. SUBMIT REPORT BUTTON
        Button(
            onClick = { viewModel.submitReport() },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = viewModel.isFormValid && !viewModel.isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
        ) {
            if (viewModel.isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Submit Report", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}

data class QuickCategoryItem(
    val category: IssueCategory,
    val label: String,
    val icon: ImageVector
)

@Composable
fun QuickCategoryTile(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) AppColorScheme.primary else AppColorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else AppColorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickReportSuccessView(
    viewModel: CommunityReportViewModel,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = Color(0xFF10B981).copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(56.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Report Submitted",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Text(
            "Your report is saved in CAMPNAV database.",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, AppColorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TRACKING REFERENCE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    viewModel.submissionResult ?: "CAM-20261012-00101",
                    fontWeight = FontWeight.Black,
                    color = AppColorScheme.primary,
                    fontSize = 22.sp
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    "Summary: ${viewModel.selectedCategory?.displayName ?: "Campus Issue"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    viewModel.derivedTitle.ifBlank { viewModel.description },
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mail, contentDescription = null, tint = AppColorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (viewModel.routingResult == "ASSIGNED_TO_LEADER")
                            "Notification queued for University Student Leader"
                        else
                            "Report saved in CAMPNAV; queued for Student Leader assignment",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
        ) {
            Text("View Report / Done", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.resetForm() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Text("Report Another Issue", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
