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
import org.com.community.components.CommunityMediaPicker
import org.com.community.components.LocationPickerMap
import org.com.community.model.IssueCategory
import org.com.community.model.IssuePriority
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityReportScreen(
    onBack: () -> Unit
) {
    val viewModel = remember { CommunityReportViewModel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REPORT COMMUNITY ISSUE", fontWeight = FontWeight.Black, fontSize = 18.sp) },
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
        containerColor = AppColorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColorScheme.background, Color(0xFF1A1A3E))
                    )
                )
                .padding(padding)
        ) {
            Crossfade(targetState = viewModel.currentStep) { step ->
                when (step) {
                    1 -> CategoryStep(viewModel)
                    2 -> DescriptionStep(viewModel)
                    3 -> LocationStep(viewModel)
                    4 -> PriorityStep(viewModel)
                    5 -> SuccessStep(viewModel, onBack)
                }
            }

            // Duplicate Detection Overlay
            if (viewModel.currentStep == 3 && viewModel.showDuplicateDialog) {
                DuplicateDetectionDialog(
                    onSupport = { viewModel.nextStep(); viewModel.nextStep() }, // Skip to success or similar
                    onContinue = { viewModel.showDuplicateDialog = false }
                )
            }

            if (viewModel.currentStep in 2..4) {
                StepProgressIndicator(
                    currentStep = viewModel.currentStep,
                    totalSteps = 4,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryStep(viewModel: CommunityReportViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "What problem are you reporting?",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Select a category to help us route your report.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(IssueCategory.entries) { category ->
                CategoryCard(
                    category = category,
                    isSelected = viewModel.selectedCategory == category,
                    onClick = {
                        viewModel.selectedCategory = category
                        viewModel.nextStep()
                    }
                )
            }
        }

        // Emergency Button
        Button(
            onClick = {
                viewModel.isEmergency = true
                viewModel.selectedCategory = IssueCategory.OTHER
                viewModel.nextStep()
            },
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("REPORT EMERGENCY", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun CategoryCard(
    category: IssueCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else AppColorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Mapping category to icons
                    val icon = when(category) {
                        IssueCategory.WATER -> Icons.Default.WaterDrop
                        IssueCategory.ROADS -> Icons.Default.AddRoad
                        IssueCategory.ELECTRICITY -> Icons.Default.ElectricBolt
                        IssueCategory.WASTE -> Icons.Default.Delete
                        IssueCategory.DRAINAGE -> Icons.Default.Waves
                        IssueCategory.ENVIRONMENT -> Icons.Default.Eco
                        IssueCategory.PUBLIC_SAFETY -> Icons.Default.Security
                        IssueCategory.BUILDINGS -> Icons.Default.Domain
                        IssueCategory.TRANSPORT -> Icons.Default.DirectionsBus
                        IssueCategory.SANITATION -> Icons.Default.CleanHands
                        IssueCategory.OTHER -> Icons.Default.MoreHoriz
                    }
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                category.displayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DescriptionStep(viewModel: CommunityReportViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Describe the issue", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Provide details to help us understand the problem.", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            placeholder = { Text("What is happening? (e.g. The water pipe burst near the bus stop)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // AI classification hint
        if (viewModel.description.contains("water") || viewModel.description.contains("leak")) {
            Surface(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AI suggests: Water Infrastructure (91% confidence)", fontSize = 12.sp, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Evidence", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("Photos and videos help verify the report faster.", color = Color.Gray, fontSize = 12.sp)

        Spacer(Modifier.height(12.dp))

        CommunityMediaPicker(
            onMediaCaptured = { viewModel.addEvidence(it) }
        ) { captureImage, pickGallery ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EvidenceButton(Icons.Default.CameraAlt, "Camera", onClick = captureImage)
                EvidenceButton(Icons.Default.PhotoLibrary, "Gallery", onClick = pickGallery)
                EvidenceButton(Icons.Default.Mic, "Voice") { 
                    // Simulate voice recording
                    viewModel.addEvidence("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                }
            }
        }

        if (viewModel.evidenceUris.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.evidenceUris) { uri ->
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removeEvidence(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = viewModel.description.length > 5,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Next: Location")
        }

        TextButton(onClick = { viewModel.prevStep() }, modifier = Modifier.fillMaxWidth()) {
            Text("Back", color = Color.Gray)
        }
    }
}

@Composable
fun EvidenceButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(80.dp).height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Text(label, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun LocationStep(viewModel: CommunityReportViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Confirm Location", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Text("Where exactly is the problem?", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LocationPickerMap(
                    modifier = Modifier.fillMaxSize(),
                    initialLatitude = viewModel.issueLocation?.latitude,
                    initialLongitude = viewModel.issueLocation?.longitude,
                    onLocationSelected = { lat, lng ->
                        viewModel.onLocationSelected(lat, lng)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = viewModel.issueLocation != null
        ) {
            Text("Next: Review")
        }

        TextButton(onClick = { viewModel.prevStep() }, modifier = Modifier.fillMaxWidth()) {
            Text("Back", color = Color.Gray)
        }
    }
}

@Composable
fun PriorityStep(viewModel: CommunityReportViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Report Severity", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Text("How urgent is this problem?", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityChip(IssuePriority.LOW, viewModel.userPriority == IssuePriority.LOW, Modifier.weight(1f)) { viewModel.userPriority = it }
            PriorityChip(IssuePriority.MEDIUM, viewModel.userPriority == IssuePriority.MEDIUM, Modifier.weight(1f)) { viewModel.userPriority = it }
            PriorityChip(IssuePriority.HIGH, viewModel.userPriority == IssuePriority.HIGH, Modifier.weight(1f)) { viewModel.userPriority = it }
            PriorityChip(IssuePriority.CRITICAL, viewModel.userPriority == IssuePriority.CRITICAL, Modifier.weight(1f)) { viewModel.userPriority = it }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { viewModel.submitReport(); viewModel.nextStep() },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !viewModel.isSubmitting
        ) {
            if (viewModel.isSubmitting) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("SUBMIT REPORT", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }

        TextButton(onClick = { viewModel.prevStep() }, modifier = Modifier.fillMaxWidth()) {
            Text("Back", color = Color.Gray)
        }
    }
}

@Composable
fun PriorityChip(priority: IssuePriority, isSelected: Boolean, modifier: Modifier = Modifier, onClick: (IssuePriority) -> Unit) {
    val color = when(priority) {
        IssuePriority.LOW -> Color(0xFF4CAF50)
        IssuePriority.MEDIUM -> Color(0xFFFFC107)
        IssuePriority.HIGH -> Color(0xFFFF9800)
        IssuePriority.CRITICAL -> Color(0xFFF44336)
    }

    Surface(
        onClick = { onClick(priority) },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else color.copy(alpha = 0.1f),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(priority.name, color = if (isSelected) Color.White else color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DuplicateDetectionDialog(onSupport: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text("SIMILAR ISSUE FOUND", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column {
                Text("A similar issue has already been reported 35 meters from this location.")
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Burst Water Pipe", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Reported 2 hours ago", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSupport) {
                Text("SUPPORT EXISTING REPORT")
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) {
                Text("CREATE NEW REPORT", color = Color.Gray)
            }
        },
        containerColor = AppColorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SuccessStep(viewModel: CommunityReportViewModel, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✅", fontSize = 64.sp)
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Report Submitted!", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
        Text(
            "Your report has been received and is being processed.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Issue ID", fontSize = 12.sp, color = Color.Gray)
                Text(viewModel.submissionResult ?: "", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 20.sp)
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Done")
        }
    }
}

@Composable
fun StepProgressIndicator(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            val step = index + 1
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (step <= currentStep) MaterialTheme.colorScheme.primary
                        else Color.Gray.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
