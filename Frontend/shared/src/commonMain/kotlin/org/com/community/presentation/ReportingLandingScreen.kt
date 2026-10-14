package org.com.community.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import kotlinx.coroutines.launch
import org.com.community.data.CommunityRepository
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingLandingScreen(
    onReportNew: () -> Unit,
    onViewMap: () -> Unit,
    onViewMyReports: () -> Unit,
    onTrackReport: () -> Unit,
    onBack: () -> Unit
) {
    val universities = listOf(
        Pair(1L, "UDSM (Dar es Salaam)"),
        Pair(2L, "UDOM (Dodoma)"),
        Pair(3L, "MUST (Mbeya)"),
        Pair(4L, "SUA (Morogoro)")
    )
    var selectedUniversityId by remember { mutableStateOf(1L) }
    var activeReportCount by remember { mutableStateOf<Int?>(null) }
    
    val repository = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedUniversityId) {
        scope.launch {
            try {
                val reports = repository.getReportsByUniversity(selectedUniversityId)
                activeReportCount = reports.count { it.status.name != "RESOLVED" && it.status.name != "CLOSED" }
            } catch (_: Exception) {
                activeReportCount = 0
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColorScheme.background,
                        Color(0xFF13132B)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Text(
                    text = "Report a Campus Issue",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Spot a problem? Report it in a few taps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Compact Campus Selector
                Text(
                    text = "CAMPUS / UNIVERSITY",
                    fontSize = 11.sp,
                    color = AppColorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    universities.forEach { (id, name) ->
                        val isSelected = selectedUniversityId == id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedUniversityId = id },
                            label = {
                                Text(
                                    name,
                                    fontSize = 12.sp,
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

                // Active Reports Banner (Shown only when real backend data is available)
                activeReportCount?.let { count ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0D47A1).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Your Campus Active Reports: $count",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Primary Action: Prominent "Report an Issue" Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = onReportNew),
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AddAlert,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Report an Issue",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Submit water, power, infrastructure & security issues under 30s",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Report Now",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Secondary Actions Header
                Text(
                    text = "MORE ACTIONS",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Secondary Action Items (Visually Quieter)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryActionRow(
                        title = "Reports",
                        subtitle = "Track campus submitted issues & status updates",
                        icon = Icons.Default.List,
                        onClick = onViewMyReports
                    )

                    SecondaryActionRow(
                        title = "Track a Report",
                        subtitle = "Search or open report by reference ID",
                        icon = Icons.Default.Search,
                        onClick = onTrackReport
                    )

                    SecondaryActionRow(
                        title = "Issues Map",
                        subtitle = "View active campus issues on map view",
                        icon = Icons.Default.Map,
                        onClick = onViewMap
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SecondaryActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
