package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import kotlinx.coroutines.launch
import org.com.community.data.CommunityRepository
import org.com.community.model.*
import org.com.core.ui.theme.AppColorScheme

enum class LeaderSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    ALL_REPORTS("All Campus Reports", Icons.Default.List),
    NEW_REPORTS("New Reports", Icons.Default.MarkAsUnread),
    AWAITING_ACK("Awaiting Acknowledgement", Icons.Default.HourglassTop),
    IN_PROGRESS("In Progress", Icons.Default.Build),
    REFERRED_TO_OFFICE("Referred to University Office", Icons.Default.Send),
    RESOLVED("Resolved Reports", Icons.Default.CheckCircle),
    ACTIVITY_HISTORY("Activity History", Icons.Default.History),
    LOGOUT("Logout", Icons.AutoMirrored.Filled.ExitToApp)
}

@Composable
fun StudentLeaderDashboardScreen(
    token: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(LeaderSection.OVERVIEW) }
    var isMobileMenuOpen by remember { mutableStateOf(false) }

    var assignedIssues by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var stats by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedIssueDetail by remember { mutableStateOf<IssueReport?>(null) }
    var referIssueTarget by remember { mutableStateOf<IssueReport?>(null) }
    var resolveIssueTarget by remember { mutableStateOf<IssueReport?>(null) }

    var officeNameInput by remember { mutableStateOf("") }
    var officeContactInput by remember { mutableStateOf("") }
    var resolutionInput by remember { mutableStateOf("") }

    val repository = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    fun loadLeaderData() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                stats = repository.getLeaderOverview(token)
                assignedIssues = repository.getLeaderAssignedIssues(token)
            } catch (e: Exception) {
                errorMessage = "Failed to load dashboard data: " + e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(token) {
        loadLeaderData()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        val isDesktop = maxWidth >= 768.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColorScheme.surfaceVariant.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isDesktop) {
                        IconButton(onClick = { isMobileMenuOpen = !isMobileMenuOpen }) {
                            Icon(Icons.Default.Menu, "Toggle Sidebar", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "UNIVERSITY STUDENT LEADER DASHBOARD",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = selectedSection.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Layout Pane
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Desktop Fixed Left Sidebar
                    if (isDesktop) {
                        Surface(
                            modifier = Modifier.width(260.dp).fillMaxHeight(),
                            color = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "STUDENT LEADER MENU",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                                )

                                LeaderSection.entries.filter { it != LeaderSection.LOGOUT }.forEach { section ->
                                    val isSelected = selectedSection == section
                                    val badgeCount = when(section) {
                                        LeaderSection.NEW_REPORTS -> assignedIssues.count { it.routingStatus == "ASSIGNED_TO_LEADER" }
                                        LeaderSection.AWAITING_ACK -> assignedIssues.count { it.routingStatus == "ASSIGNED_TO_LEADER" }
                                        LeaderSection.IN_PROGRESS -> assignedIssues.count { it.status == IssueStatus.IN_PROGRESS }
                                        LeaderSection.REFERRED_TO_OFFICE -> assignedIssues.count { it.routingStatus == "REFERRED_TO_UNIVERSITY" }
                                        else -> 0
                                    }

                                    SidebarNavItem(
                                        title = section.title,
                                        icon = section.icon,
                                        isSelected = isSelected,
                                        badgeCount = badgeCount,
                                        onClick = { selectedSection = section }
                                    )
                                }

                                Spacer(Modifier.weight(1f))

                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                                SidebarNavItem(
                                    title = "Logout",
                                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                                    isSelected = false,
                                    onClick = onLogout
                                )
                            }
                        }
                    }

                    // Main Content Pane
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(20.dp)) {
                        if (isLoading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AppColorScheme.primary)
                            }
                        } else {
                            val filteredReports = when (selectedSection) {
                                LeaderSection.NEW_REPORTS -> assignedIssues.filter { it.routingStatus == "ASSIGNED_TO_LEADER" }
                                LeaderSection.AWAITING_ACK -> assignedIssues.filter { it.routingStatus == "ASSIGNED_TO_LEADER" }
                                LeaderSection.IN_PROGRESS -> assignedIssues.filter { it.status == IssueStatus.IN_PROGRESS }
                                LeaderSection.REFERRED_TO_OFFICE -> assignedIssues.filter { it.routingStatus == "REFERRED_TO_UNIVERSITY" }
                                LeaderSection.RESOLVED -> assignedIssues.filter { it.status == IssueStatus.RESOLVED || it.status == IssueStatus.COMMUNITY_VERIFIED }
                                else -> assignedIssues
                            }

                            when (selectedSection) {
                                LeaderSection.OVERVIEW -> LeaderOverviewContent(
                                    stats = stats,
                                    recentIssues = assignedIssues.take(5),
                                    onViewIssue = { selectedIssueDetail = it },
                                    onNavigateSection = { selectedSection = it }
                                )
                                else -> LeaderReportsList(
                                    reports = filteredReports,
                                    onAcknowledge = { issue ->
                                        scope.launch {
                                            repository.leaderAcknowledgeReport(token, issue.id)
                                            loadLeaderData()
                                        }
                                    },
                                    onRefer = { issue -> referIssueTarget = issue },
                                    onResolve = { issue -> resolveIssueTarget = issue },
                                    onViewDetail = { issue -> selectedIssueDetail = issue },
                                    onDelete = { issue ->
                                        scope.launch {
                                            repository.deleteLeaderIssue(token, issue.id)
                                            loadLeaderData()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Mobile Drawer
                if (!isDesktop && isMobileMenuOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { isMobileMenuOpen = false }
                    ) {
                        Surface(
                            modifier = Modifier.width(280.dp).fillMaxHeight(),
                            color = AppColorScheme.surfaceVariant,
                            shadowElevation = 16.dp
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("LEADER MENU", fontWeight = FontWeight.Bold, color = Color.White)
                                    IconButton(onClick = { isMobileMenuOpen = false }) {
                                        Icon(Icons.Default.Close, null, tint = Color.White)
                                    }
                                }

                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                                LeaderSection.entries.filter { it != LeaderSection.LOGOUT }.forEach { section ->
                                    SidebarNavItem(
                                        title = section.title,
                                        icon = section.icon,
                                        isSelected = selectedSection == section,
                                        onClick = {
                                            selectedSection = section
                                            isMobileMenuOpen = false
                                        }
                                    )
                                }

                                Spacer(Modifier.weight(1f))

                                SidebarNavItem(
                                    title = "Logout",
                                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                                    isSelected = false,
                                    onClick = onLogout
                                )
                            }
                        }
                    }
                }
            }
        }

        // Referral Dialog
        referIssueTarget?.let { issue ->
            AlertDialog(
                onDismissRequest = { referIssueTarget = null },
                title = { Text("Refer Issue to University Office", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Report Ref: ${issue.id} (${issue.category.displayName})", fontSize = 12.sp, color = AppColorScheme.primary)
                        OutlinedTextField(
                            value = officeNameInput,
                            onValueChange = { officeNameInput = it },
                            label = { Text("University Office / Department Name", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = officeContactInput,
                            onValueChange = { officeContactInput = it },
                            label = { Text("Office Email or Phone", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (officeNameInput.isNotBlank()) {
                                scope.launch {
                                    repository.leaderReferToOffice(
                                        token, issue.id, ReferralRequestDTO(officeName = officeNameInput, officeContact = officeContactInput)
                                    )
                                    referIssueTarget = null
                                    officeNameInput = ""
                                    officeContactInput = ""
                                    loadLeaderData()
                                }
                            }
                        }
                    ) {
                        Text("Send Referral")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { referIssueTarget = null }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = AppColorScheme.surface
            )
        }

        // Resolution Dialog
        resolveIssueTarget?.let { issue ->
            AlertDialog(
                onDismissRequest = { resolveIssueTarget = null },
                title = { Text("Record Issue Resolution", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Report Ref: ${issue.id}", fontSize = 12.sp, color = AppColorScheme.primary)
                        OutlinedTextField(
                            value = resolutionInput,
                            onValueChange = { resolutionInput = it },
                            label = { Text("Resolution Details & Evidence Summary", color = Color.Gray) },
                            modifier = Modifier.height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resolutionInput.isNotBlank()) {
                                scope.launch {
                                    repository.leaderResolveReport(token, issue.id, resolutionInput, null)
                                    resolveIssueTarget = null
                                    resolutionInput = ""
                                    loadLeaderData()
                                }
                            }
                        }
                    ) {
                        Text("Confirm Resolution")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resolveIssueTarget = null }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = AppColorScheme.surface
            )
        }

        // Detail Dialog
        selectedIssueDetail?.let { issue ->
            AlertDialog(
                onDismissRequest = { selectedIssueDetail = null },
                title = { Text("Report Details: ${issue.id}", fontWeight = FontWeight.Bold, color = AppColorScheme.primary) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("University: ${issue.universityName ?: "Assigned University"}", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Category: ${issue.category.displayName}", color = Color.White)
                        Text("Urgency: ${issue.urgency}", color = Color.Yellow)
                        Text("Status: ${issue.status.name} (Routing: ${issue.routingStatus})", color = AppColorScheme.secondary)
                        Text("Description:", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(issue.description, color = Color.White)
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedIssueDetail = null }) {
                        Text("Close")
                    }
                },
                containerColor = AppColorScheme.surface
            )
        }
    }
}

@Composable
fun LeaderOverviewContent(
    stats: Map<String, Any>,
    recentIssues: List<IssueReport>,
    onViewIssue: (IssueReport) -> Unit,
    onNavigateSection: (LeaderSection) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Overview Statistics", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            LeaderStatCard("Assigned Reports", (stats["totalReports"] ?: 0).toString(), Icons.Default.List, AppColorScheme.primary, Modifier.weight(1f))
            LeaderStatCard("Awaiting Ack", (stats["awaitingAcknowledgement"] ?: 0).toString(), Icons.Default.HourglassTop, Color(0xFFFF9800), Modifier.weight(1f))
            LeaderStatCard("In Progress", (stats["inProgress"] ?: 0).toString(), Icons.Default.Build, Color(0xFF2196F3), Modifier.weight(1f))
            LeaderStatCard("Resolved", (stats["resolvedReports"] ?: 0).toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50), Modifier.weight(1f))
        }

        Spacer(Modifier.height(32.dp))

        Text("Recent Assigned Campus Reports", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (recentIssues.isEmpty()) {
            Text("No reports currently assigned.", color = Color.Gray, fontSize = 14.sp)
        } else {
            recentIssues.forEach { issue ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onViewIssue(issue) },
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(issue.id + " — " + issue.category.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(issue.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = AppColorScheme.primary.copy(alpha = 0.2f)) {
                            Text(issue.routingStatus, color = AppColorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderReportsList(
    reports: List<IssueReport>,
    onAcknowledge: (IssueReport) -> Unit,
    onRefer: (IssueReport) -> Unit,
    onResolve: (IssueReport) -> Unit,
    onViewDetail: (IssueReport) -> Unit,
    onDelete: (IssueReport) -> Unit
) {
    var reportToDelete by remember { mutableStateOf<IssueReport?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(reports) { issue ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(issue.id, fontWeight = FontWeight.Black, color = AppColorScheme.primary, fontSize = 16.sp)
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF3B82F6).copy(alpha = 0.2f)) {
                                Text(issue.routingStatus, color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(issue.category.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(issue.description, color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (issue.routingStatus == "ASSIGNED_TO_LEADER") {
                                Button(
                                    onClick = { onAcknowledge(issue) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Acknowledge in CAMPNAV", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = { onRefer(issue) },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Refer to Office", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onResolve(issue) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Record Resolution", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onViewDetail(issue) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Details", fontSize = 11.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { reportToDelete = issue },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        reportToDelete?.let { issue ->
            AlertDialog(
                onDismissRequest = { reportToDelete = null },
                title = { Text("Confirm Delete Report", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to delete report ${issue.id}? This action cannot be undone.", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete(issue)
                            reportToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reportToDelete = null }) { Text("Cancel", color = Color.Gray) }
                },
                containerColor = AppColorScheme.surface
            )
        }
    }
}

@Composable
fun LeaderStatCard(title: String, count: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
