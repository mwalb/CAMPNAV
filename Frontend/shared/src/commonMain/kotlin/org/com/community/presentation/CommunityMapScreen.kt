package org.com.community.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.community.model.*
import org.com.community.data.CommunityRepository
import org.com.core.ui.theme.AppColorScheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMapScreen(
    onBack: () -> Unit
) {
    val repository = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()
    var nearbyIssues by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<IssueCategory?>(null) }
    var showRadar by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Mock user location for radar demonstration
                nearbyIssues = repository.getNearbyIssues(-6.7924, 39.2083)
            } catch (e: Exception) {
                // Fallback mock data if server not available
                nearbyIssues = listOf(
                    IssueReport(
                        id = "JMF-123", category = IssueCategory.WATER, subCategory = "Leakage",
                        description = "Water Leakage", issueLocation = GeoLocation(0.0, 0.0),
                        reporterLocation = GeoLocation(0.0, 0.0), distanceToIssue = 120.0,
                        distanceText = "120m away", evidenceUris = emptyList(),
                        userPriority = IssuePriority.HIGH, calculatedPriority = IssuePriority.HIGH,
                        impactScore = 70, reporterId = "", timestamp = 0, lastUpdated = 0
                    )
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Map Placeholder
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color.Gray.copy(alpha = 0.2f))
            Text("Community Intelligence Map", color = Color.Gray, fontWeight = FontWeight.Bold)
            Text("Real-time Issue Visualization", color = Color.Gray.copy(alpha = 0.5f), fontSize = 12.sp)
        }

        // Top Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(AppColorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }

            Surface(
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = AppColorScheme.surface.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Search Area...", color = Color.Gray, fontSize = 14.sp)
                }
            }

            IconButton(
                onClick = { /* Toggle Heatmap */ },
                modifier = Modifier.clip(CircleShape).background(AppColorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White)
            }
        }

        // Category Filter
        LazyRow(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All Issues") },
                    colors = FilterChipDefaults.filterChipColors(labelColor = Color.White)
                )
            }
            items(IssueCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.displayName) },
                    colors = FilterChipDefaults.filterChipColors(labelColor = Color.White)
                )
            }
        }

        // Community Incident Radar
        AnimatedVisibility(
            visible = showRadar,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INCIDENT RADAR", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                        IconButton(onClick = { showRadar = false }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (nearbyIssues.isEmpty()) {
                        Text("No active issues detected within 2km.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }

                    nearbyIssues.take(3).forEachIndexed { index, issue ->
                        RadarItem(
                            title = issue.description.take(25) + (if (issue.description.length > 25) "..." else ""),
                            distance = issue.distanceText,
                            color = when(issue.category) {
                                IssueCategory.WATER -> Color(0xFF00E5FF)
                                IssueCategory.WASTE -> Color(0xFFFFC107)
                                IssueCategory.PUBLIC_SAFETY -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            icon = when(issue.category) {
                                IssueCategory.WATER -> Icons.Default.WaterDrop
                                IssueCategory.WASTE -> Icons.Default.Delete
                                IssueCategory.PUBLIC_SAFETY -> Icons.Default.Security
                                else -> Icons.Default.Report
                            }
                        )
                        if (index < nearbyIssues.size - 1 && index < 2) {
                            Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { /* Open Report Screen */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("REPORT SOMETHING NEARBY")
                    }
                }
            }
        }

        if (!showRadar) {
            FloatingActionButton(
                onClick = { showRadar = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Radar, contentDescription = "Open Radar")
            }
        }
    }
}

@Composable
fun RadarItem(title: String, distance: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(distance, fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { /* Navigate */ }) {
            Text("NAVIGATE", fontWeight = FontWeight.Bold)
        }
    }
}
