package org.com.community.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.com.community.components.ProblemTimeline
import org.com.community.data.CommunityRepository
import org.com.community.model.*
import org.com.campus.utils.openGoogleMapsNavigation
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String,
    onBack: () -> Unit
) {
    val repository = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()
    var issue by remember { mutableStateOf<IssueReport?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var confirmationMessage by remember { mutableStateOf<String?>(null) }

    fun loadIssue() {
        scope.launch {
            try {
                issue = repository.getIssueDetails(issueId) ?: repository.trackReport(issueId)
            } catch (e: Exception) {
                issue = null
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(issueId) {
        loadIssue()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(issue?.id ?: issueId, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else if (issue == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Report not found.", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            } else {
                val currentIssue = issue!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Status Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(currentIssue.status)
                        if (currentIssue.isEmergency) {
                            Badge(containerColor = Color(0xFFFF4444)) {
                                Text("EMERGENCY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        currentIssue.category.displayName.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 12.sp
                    )

                    Text(
                        currentIssue.description,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (!currentIssue.publicResponse.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Official Resolution / Response", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(currentIssue.publicResponse!!, color = Color.White)
                            }
                        }
                    }

                    // Evidence Display
                    if (currentIssue.evidenceUris.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(currentIssue.evidenceUris) { uri ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentIssue.issueLocation.address ?: "Location Coordinates", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Lat: ${currentIssue.issueLocation.latitude}, Lng: ${currentIssue.issueLocation.longitude}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    openGoogleMapsNavigation(
                                        currentIssue.reporterLocation.latitude,
                                        currentIssue.reporterLocation.longitude,
                                        currentIssue.issueLocation.latitude,
                                        currentIssue.issueLocation.longitude
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("DIRECTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Stats Row (Priority & Impact & Confirmations)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailStatCard("PRIORITY", currentIssue.calculatedPriority.name, when(currentIssue.calculatedPriority) {
                            IssuePriority.LOW -> Color(0xFF4CAF50)
                            IssuePriority.MEDIUM -> Color(0xFFFFC107)
                            IssuePriority.HIGH -> Color(0xFFFF9800)
                            IssuePriority.CRITICAL -> Color(0xFFF44336)
                        }, Modifier.weight(1f))

                        DetailStatCard("CONFIRMATIONS", "${currentIssue.supportsCount} people", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Is issue still happening? Confirmation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Is this issue still happening?", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val updated = repository.confirmIssue(currentIssue.id)
                                            if (updated != null) {
                                                issue = updated
                                                confirmationMessage = "Thank you! Confirmation recorded."
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Yes, still happening")
                                }
                            }
                            if (confirmationMessage != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(confirmationMessage!!, color = Color(0xFF4CAF50), fontSize = 12.sp)
                            }
                        }
                    }

                    // Timeline Section
                    Text(
                        "PROBLEM TIMELINE",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    ProblemTimeline(
                        events = currentIssue.timeline,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    // Resolution Verification Card
                    if (currentIssue.status == IssueStatus.RESOLVED) {
                        VerificationCard(
                            onVerify = { resolved ->
                                scope.launch {
                                    val updated = repository.verifyIssue(currentIssue.id, resolved)
                                    if (updated != null) {
                                        issue = updated
                                    }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
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
fun VerificationCard(onVerify: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("COMMUNITY VERIFICATION", fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
            Text(
                "Has this issue actually been resolved? Your verification helps complete the resolution lifecycle.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onVerify(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("YES, IT'S FIXED")
                }
                OutlinedButton(
                    onClick = { onVerify(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4444))
                ) {
                    Text("STILL HAPPENING")
                }
            }
        }
    }
}
