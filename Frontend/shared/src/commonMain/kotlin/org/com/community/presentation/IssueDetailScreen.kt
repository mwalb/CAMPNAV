package org.com.community.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.com.community.components.ProblemTimeline
import org.com.community.model.*
import org.com.campus.utils.openGoogleMapsNavigation
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String,
    onBack: () -> Unit
) {
    // Mock issue data fetch
    val issue = remember {
        IssueReport(
            id = issueId,
            category = IssueCategory.WATER,
            description = "Major burst pipe near the student hostel. Water is flooding the walkway.",
            issueLocation = GeoLocation(-6.7924, 39.2083, address = "University Road, Block 4"),
            reporterLocation = GeoLocation(-6.7930, 39.2090),
            distanceToIssue = 120.0,
            distanceText = "120m away",
            evidenceUris = emptyList(),
            userPriority = IssuePriority.HIGH,
            calculatedPriority = IssuePriority.CRITICAL,
            impactScore = 85,
            reporterId = "user_123",
            timestamp = 1725000000000L,
            lastUpdated = 1725005000000L,
            status = IssueStatus.IN_PROGRESS,
            assignedDepartment = "Water Works Dept",
            timeline = listOf(
                IssueTimelineEvent(1725000000000L, IssueStatus.SUBMITTED, "Report submitted by resident"),
                IssueTimelineEvent(1725001000000L, IssueStatus.VERIFIED, "Location verified via GPS"),
                IssueTimelineEvent(1725002000000L, IssueStatus.ASSIGNED, "Assigned to Water Works Dept"),
                IssueTimelineEvent(1725005000000L, IssueStatus.IN_PROGRESS, "Officer on site, repair started")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(issue.id, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
            // Status Header
            StatusBadge(issue.status)

            Spacer(Modifier.height(16.dp))

            Text(
                issue.category.displayName.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 12.sp
            )

            Text(
                issue.description,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Evidence Display
            if (issue.evidenceUris.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(issue.evidenceUris) { uri ->
                        Card(
                            modifier = Modifier.size(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Location Info
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(issue.issueLocation.address ?: "Unknown Location", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(issue.distanceText, color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            openGoogleMapsNavigation(
                                issue.reporterLocation.latitude,
                                issue.reporterLocation.longitude,
                                issue.issueLocation.latitude,
                                issue.issueLocation.longitude
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("DIRECTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Stats Row (Priority & Impact)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailStatCard("PRIORITY", issue.calculatedPriority.name, when(issue.calculatedPriority) {
                    IssuePriority.LOW -> Color(0xFF4CAF50)
                    IssuePriority.MEDIUM -> Color(0xFFFFC107)
                    IssuePriority.HIGH -> Color(0xFFFF9800)
                    IssuePriority.CRITICAL -> Color(0xFFF44336)
                }, Modifier.weight(1f))

                DetailStatCard("IMPACT SCORE", "${issue.impactScore}/100", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }

            // Timeline Section
            Text(
                "PROBLEM TIMELINE",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ProblemTimeline(
                events = issue.timeline,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp)
            )

            // Verification Action
            if (issue.status == IssueStatus.RESOLVED) {
                VerificationCard()
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatusBadge(status: IssueStatus) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun VerificationCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("RESOLVED?", fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
            Text(
                "Has this issue actually been resolved? Your verification helps us maintain accuracy.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { /* Yes */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("YES")
                }
                OutlinedButton(
                    onClick = { /* No */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("NO")
                }
            }
        }
    }
}
