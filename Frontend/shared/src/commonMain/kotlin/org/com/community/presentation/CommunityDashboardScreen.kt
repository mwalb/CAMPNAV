package org.com.community.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.com.community.model.*
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDashboardScreen(
    userRole: UserRole,
    onBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onReportSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("COMMUNITY DASHBOARD", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(Icons.Default.Map, contentDescription = "Map View")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = AppColorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("TOTAL", "1,284", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatCard("OPEN", "327", Color(0xFFFF9800), Modifier.weight(1f))
                StatCard("CRITICAL", "18", Color(0xFFF44336), Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Text("Recent Reports", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // Mock Data for reports
            val mockReports = remember {
                listOf(
                    IssueReportStub("JMF-000184", IssueCategory.WATER, "Burst pipe near hostel", IssueStatus.ASSIGNED, IssuePriority.CRITICAL, "https://images.unsplash.com/photo-1590483734724-38fa19dd780c?auto=format&fit=crop&w=400&q=80"),
                    IssueReportStub("JMF-000185", IssueCategory.ROADS, "Large pothole entrance", IssueStatus.IN_PROGRESS, IssuePriority.HIGH, "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&w=400&q=80"),
                    IssueReportStub("JMF-000186", IssueCategory.WASTE, "Illegal dumping", IssueStatus.RECEIVED, IssuePriority.MEDIUM, "https://images.unsplash.com/photo-1530587191325-3db32d826c18?auto=format&fit=crop&w=400&q=80")
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(mockReports) { report ->
                    ReportListItem(report, onClick = { onReportSelected(report.id) })
                }
            }
        }
    }
}

data class IssueReportStub(
    val id: String,
    val category: IssueCategory,
    val title: String,
    val status: IssueStatus,
    val priority: IssuePriority,
    val thumbnail: String? = null
)

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 24.sp, color = color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ReportListItem(report: IssueReportStub, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (report.thumbnail != null) {
                AsyncImage(
                    model = report.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(report.id, fontSize = 10.sp, color = Color.Gray)
                Text(report.title, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(report.category) {
                            IssueCategory.WATER -> Icons.Default.WaterDrop
                            IssueCategory.ROADS -> Icons.Default.AddRoad
                            IssueCategory.WASTE -> Icons.Default.Delete
                            else -> Icons.Default.Category
                        },
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(report.category.displayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Badge(
                    containerColor = when(report.priority) {
                        IssuePriority.LOW -> Color(0xFF4CAF50)
                        IssuePriority.MEDIUM -> Color(0xFFFFC107)
                        IssuePriority.HIGH -> Color(0xFFFF9800)
                        IssuePriority.CRITICAL -> Color(0xFFF44336)
                    }
                ) {
                    Text(report.priority.name, color = Color.White, fontSize = 8.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(report.status.name, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
