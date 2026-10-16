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

enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    COMMUNITY_ISSUES("Community Issues", Icons.Default.ReportProblem),
    STUDENT_LEADERS("University Student Leaders", Icons.Default.Groups),
    ISSUE_NOTIFICATIONS("Issue Notifications", Icons.Default.Mail),
    ROUTING_ESCALATIONS("Routing & Escalations", Icons.Default.AltRoute),
    COMMUNITY_REVIEWS("Community Reviews", Icons.Default.RateReview),
    USER_FEEDBACK("User Feedback", Icons.Default.Feedback),
    LOCATION_CONTRIBUTIONS("Location Contributions", Icons.Default.AddLocation),
    ACTIVITY_LOGS("Activity Logs", Icons.Default.History)
}

@Composable
fun AdminDashboardScreen(
    token: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
    var isMobileMenuOpen by remember { mutableStateOf(false) }

    var contributions by remember { mutableStateOf(emptyList<CommunityContribution>()) }
    var reviews by remember { mutableStateOf(emptyList<CommunityReview>()) }
    var feedbacks by remember { mutableStateOf(emptyList<UserFeedback>()) }
    var logs by remember { mutableStateOf(emptyList<AdminActivityLog>()) }
    var issues by remember { mutableStateOf(emptyList<IssueReport>()) }
    var leaders by remember { mutableStateOf(emptyList<StudentLeaderDTO>()) }
    var notifications by remember { mutableStateOf(emptyList<EmailNotificationDTO>()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showAppointLeaderDialog by remember { mutableStateOf(false) }
    var appointUnivId by remember { mutableStateOf(1L) }
    var appointLeaderName by remember { mutableStateOf("") }
    var appointLeaderEmail by remember { mutableStateOf("") }
    var appointLeaderUsername by remember { mutableStateOf("") }
    var appointValidationError by remember { mutableStateOf<String?>(null) }

    val communityRepo = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    fun loadData() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                contributions = communityRepo.getAdminContributions(token)
                reviews = communityRepo.getAdminReviews(token)
                feedbacks = communityRepo.getAllFeedback(token)
                logs = communityRepo.getAdminLogs(token)
                issues = communityRepo.getAdminIssues(token)
                leaders = communityRepo.getAdminLeaders(token)
                notifications = communityRepo.getAdminNotifications(token)
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("401") || msg.contains("403") || msg.contains("Unauthorized") || msg.contains("Forbidden")) {
                    onLogout()
                } else {
                    errorMessage = "Failed to load admin data: ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(token) {
        loadData()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(AppColorScheme.background)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isDesktop) {
                        IconButton(onClick = { isMobileMenuOpen = !isMobileMenuOpen }) {
                            Icon(Icons.Default.Menu, "Toggle Menu", tint = Color.White)
                        }
                    }

                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }

                    Text("CAMPNAV ADMIN DASHBOARD", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)

                    Spacer(Modifier.weight(1f))

                    Text(selectedSection.title, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            // Body Layout
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isDesktop) {
                        Surface(
                            modifier = Modifier.width(260.dp).fillMaxHeight(),
                            color = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("ADMIN SECTIONS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)

                                AdminSection.entries.forEach { section ->
                                    val isSelected = selectedSection == section
                                    val badge = when (section) {
                                        AdminSection.COMMUNITY_ISSUES -> issues.count { it.status == IssueStatus.SUBMITTED }
                                        AdminSection.ROUTING_ESCALATIONS -> issues.count { it.routingStatus == "UNASSIGNED" }
                                        AdminSection.ISSUE_NOTIFICATIONS -> notifications.count { it.deliveryState == "FAILED" }
                                        else -> 0
                                    }

                                    SidebarNavItem(
                                        title = section.title,
                                        icon = section.icon,
                                        isSelected = isSelected,
                                        badgeCount = badge,
                                        onClick = { selectedSection = section }
                                    )
                                }

                                Spacer(Modifier.weight(1f))
                                SidebarNavItem("Logout", Icons.AutoMirrored.Filled.ExitToApp, false, onClick = onLogout)
                            }
                        }
                    }

                    // Content Area
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(20.dp)) {
                        if (isLoading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AppColorScheme.primary)
                            }
                        } else {
                            when (selectedSection) {
                                AdminSection.OVERVIEW -> OverviewContent(issues, contributions, reviews, feedbacks, logs) { selectedSection = it }
                                AdminSection.COMMUNITY_ISSUES -> IssuesContent(
                                    issues = issues,
                                    onUpdateStatus = { id, status, msg -> scope.launch { communityRepo.adminUpdateStatus(token, id, status, msg, null, null); loadData() } },
                                    onResolve = {},
                                    onResendNotification = { externalId -> scope.launch { communityRepo.resendLeaderNotification(token, externalId); loadData() } },
                                    onDeleteIssue = { externalId -> scope.launch { communityRepo.deleteAdminIssue(token, externalId); loadData() } }
                                )
                                AdminSection.STUDENT_LEADERS -> StudentLeadersAdminContent(
                                    leaders = leaders,
                                    onAppointNew = { showAppointLeaderDialog = true },
                                    onUpdateStatus = { id, status -> scope.launch { communityRepo.updateLeaderStatus(token, id, status, null); loadData() } },
                                    onDeleteLeader = { id -> scope.launch { communityRepo.deleteLeaderAccount(token, id); loadData() } }
                                )
                                AdminSection.ISSUE_NOTIFICATIONS -> NotificationsAdminContent(
                                    notifications = notifications,
                                    onRetry = { id -> scope.launch { communityRepo.retryNotification(token, id); loadData() } }
                                )
                                AdminSection.ROUTING_ESCALATIONS -> EscalationsAdminContent(
                                    unassignedIssues = issues.filter { it.routingStatus == "UNASSIGNED" },
                                    onAppointLeader = { showAppointLeaderDialog = true }
                                )
                                AdminSection.COMMUNITY_REVIEWS -> ReviewsContent(
                                    reviews = reviews,
                                    onApprove = { id -> scope.launch { communityRepo.approveReview(token, id); loadData() } },
                                    onReject = { id, reason -> scope.launch { communityRepo.rejectReview(token, id, reason); loadData() } },
                                    onDelete = { id -> scope.launch { communityRepo.deleteReview(token, id); loadData() } }
                                )
                                AdminSection.USER_FEEDBACK -> FeedbackContent(
                                    feedbacks = feedbacks,
                                    onApprove = { id -> scope.launch { communityRepo.approveFeedback(token, id); loadData() } },
                                    onReject = { id, reason -> scope.launch { communityRepo.rejectFeedback(token, id, reason); loadData() } },
                                    onDelete = { id -> scope.launch { communityRepo.deleteFeedback(token, id); loadData() } }
                                )
                                AdminSection.LOCATION_CONTRIBUTIONS -> ContributionsContent(contributions, { id -> scope.launch { communityRepo.approveContribution(token, id); loadData() } }, { id, reason -> scope.launch { communityRepo.rejectContribution(token, id, reason); loadData() } })
                                AdminSection.ACTIVITY_LOGS -> LogsContent(logs)
                            }
                        }
                    }
                }
            }
        }

        // Appoint Leader Dialog
        if (showAppointLeaderDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAppointLeaderDialog = false
                    appointValidationError = null
                },
                title = { Text("Appoint University Student Leader", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select University *", color = Color.Gray, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(Pair(1L, "UDSM"), Pair(2L, "UDOM"), Pair(3L, "MUST"), Pair(4L, "SUA")).forEach { (id, code) ->
                                FilterChip(
                                    selected = appointUnivId == id,
                                    onClick = { appointUnivId = id },
                                    label = { Text(code) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = appointLeaderName,
                            onValueChange = {
                                appointLeaderName = it
                                appointValidationError = null
                            },
                            label = { Text("Full Name *", color = Color.Gray) },
                            placeholder = { Text("e.g. John K. Doe", color = Color.DarkGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = appointLeaderEmail,
                            onValueChange = {
                                appointLeaderEmail = it
                                appointValidationError = null
                            },
                            label = { Text("Official Email Address *", color = Color.Gray) },
                            placeholder = { Text("e.g. leader@university.ac.tz", color = Color.DarkGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = appointLeaderUsername,
                            onValueChange = { appointLeaderUsername = it },
                            label = { Text("Username (Optional)", color = Color.Gray) },
                            placeholder = { Text("e.g. udsm_leader", color = Color.DarkGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        appointValidationError?.let { err ->
                            Text(err, color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nameTrimmed = appointLeaderName.trim()
                            val emailTrimmed = appointLeaderEmail.trim()
                            if (nameTrimmed.isBlank()) {
                                appointValidationError = "Full name is required."
                                return@Button
                            }
                            if (emailTrimmed.isBlank() || !emailTrimmed.contains("@") || !emailTrimmed.contains(".")) {
                                appointValidationError = "A valid email address is required."
                                return@Button
                            }

                            scope.launch {
                                val res = communityRepo.appointLeader(
                                    token = token,
                                    universityId = appointUnivId,
                                    fullName = nameTrimmed,
                                    email = emailTrimmed.lowercase(),
                                    leaderUsername = appointLeaderUsername.trim().ifBlank { null }
                                )
                                if (res != null) {
                                    showAppointLeaderDialog = false
                                    appointLeaderName = ""
                                    appointLeaderEmail = ""
                                    appointLeaderUsername = ""
                                    appointValidationError = null
                                    loadData()
                                } else {
                                    appointValidationError = "Failed to appoint leader. Please check parameters and try again."
                                }
                            }
                        }
                    ) {
                        Text("Appoint Leader")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAppointLeaderDialog = false
                        appointValidationError = null
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = AppColorScheme.surface
            )
        }
    }
}

@Composable
fun StudentLeadersAdminContent(
    leaders: List<StudentLeaderDTO>,
    onAppointNew: () -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onDeleteLeader: (Long) -> Unit
) {
    var leaderToDelete by remember { mutableStateOf<StudentLeaderDTO?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("University Student Leaders (${leaders.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Button(onClick = onAppointNew, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.PersonAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Appoint New Leader")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (leaders.isEmpty()) {
                Text("No student leaders appointed.", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(leaders) { leader ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(leader.leaderFullName + " (@" + leader.leaderUsername + ")", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    Text("University: " + leader.universityName, color = AppColorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Email: " + leader.leaderEmail, color = Color.Gray, fontSize = 12.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (leader.status == "ACTIVE") Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                ) {
                                    Text(leader.status, color = if (leader.status == "ACTIVE") Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(6.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                if (leader.status == "ACTIVE" && leader.id != null) {
                                    OutlinedButton(onClick = { onUpdateStatus(leader.id, "DEACTIVATED") }) {
                                        Text("Deactivate", fontSize = 11.sp, color = Color(0xFFEF4444))
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                if (leader.id != null) {
                                    OutlinedButton(onClick = { leaderToDelete = leader }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))) {
                                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        leaderToDelete?.let { leader ->
            AlertDialog(
                onDismissRequest = { leaderToDelete = null },
                title = { Text("Confirm Delete University Leader", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to delete/deactivate University Leader account for ${leader.leaderFullName} (${leader.universityName})?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            leader.id?.let { onDeleteLeader(it) }
                            leaderToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { leaderToDelete = null }) { Text("Cancel", color = Color.Gray) }
                },
                containerColor = AppColorScheme.surface
            )
        }
    }
}

@Composable
fun NotificationsAdminContent(
    notifications: List<EmailNotificationDTO>,
    onRetry: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Email Notification Queue & Outbox (${notifications.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Text("No email notification records in outbox.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.subject, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("To: ${notif.recipientEmail} (${notif.recipientRole}) | Type: ${notif.notificationType}", color = Color.Gray, fontSize = 12.sp)
                                if (!notif.lastError.isNullOrBlank()) {
                                    Text("Error: ${notif.lastError}", color = Color(0xFFEF4444), fontSize = 11.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (notif.deliveryState == "DELIVERED" || notif.deliveryState == "SENT_TO_PROVIDER") Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                            ) {
                                Text(notif.deliveryState, color = if (notif.deliveryState == "DELIVERED" || notif.deliveryState == "SENT_TO_PROVIDER") Color(0xFF10B981) else Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                            }
                            if (notif.deliveryState == "FAILED") {
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { onRetry(notif.id) }) {
                                    Text("Retry", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EscalationsAdminContent(
    unassignedIssues: List<IssueReport>,
    onAppointLeader: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Unrouted & Unassigned Reports (${unassignedIssues.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (unassignedIssues.isEmpty()) {
            Text("All reports have been successfully routed to active Student Leaders.", color = Color(0xFF10B981))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(unassignedIssues) { issue ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(issue.id + " — " + issue.category.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("University: " + (issue.universityName ?: "Unassigned Campus"), color = AppColorScheme.primary, fontSize = 12.sp)
                                Text(issue.description, color = Color.Gray, fontSize = 12.sp)
                            }
                            Button(onClick = onAppointLeader, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                                Text("Appoint Leader", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = if (isSelected) AppColorScheme.primary else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, color = if (isSelected) Color.White else Color.LightGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.weight(1f))
            if (badgeCount > 0) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFB74D)) {
                    Text(badgeCount.toString(), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun OverviewContent(
    issues: List<IssueReport>,
    contributions: List<CommunityContribution>,
    reviews: List<CommunityReview>,
    feedbacks: List<UserFeedback>,
    logs: List<AdminActivityLog>,
    onNavigateSection: (AdminSection) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Dashboard Overview", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Summary of campus issues, student leaders, notifications & contributions", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Community Issues", issues.size.toString(), issues.count { it.status == IssueStatus.SUBMITTED }, Icons.Default.ReportProblem, Color(0xFF00E5FF), Modifier.weight(1f)) { onNavigateSection(AdminSection.COMMUNITY_ISSUES) }
                MetricCard("Contributions", contributions.size.toString(), contributions.count { it.status == "PENDING" }, Icons.Default.AddLocation, AppColorScheme.primary, Modifier.weight(1f)) { onNavigateSection(AdminSection.LOCATION_CONTRIBUTIONS) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Reviews", reviews.size.toString(), reviews.count { it.status == "PENDING" }, Icons.Default.RateReview, Color(0xFF9C27B0), Modifier.weight(1f)) { onNavigateSection(AdminSection.COMMUNITY_REVIEWS) }
                MetricCard("Feedback", feedbacks.size.toString(), feedbacks.count { it.status == "PENDING" }, Icons.Default.Feedback, Color(0xFFFFB74D), Modifier.weight(1f)) { onNavigateSection(AdminSection.USER_FEEDBACK) }
            }
        }
    }
}

@Composable
fun IssuesContent(
    issues: List<IssueReport>,
    onUpdateStatus: (String, String, String) -> Unit,
    onResolve: (IssueReport) -> Unit,
    onResendNotification: (String) -> Unit,
    onDeleteIssue: (String) -> Unit
) {
    var selectedIssue by remember { mutableStateOf<IssueReport?>(null) }
    var issueToDelete by remember { mutableStateOf<IssueReport?>(null) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Community Issues (${issues.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }

            notificationMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(msg, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                }
            }

            if (issues.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No issue reports registered.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(issues) { issue ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(issue.id, fontWeight = FontWeight.Bold, color = AppColorScheme.primary, fontSize = 14.sp)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AppColorScheme.primary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                issue.category.displayName,
                                                color = AppColorScheme.primary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (issue.status) {
                                            IssueStatus.RESOLVED, IssueStatus.COMMUNITY_VERIFIED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                            IssueStatus.IN_PROGRESS, IssueStatus.UNDER_REVIEW -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                            IssueStatus.REJECTED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                            else -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                                        }
                                    ) {
                                        Text(
                                            issue.status.name,
                                            color = when (issue.status) {
                                                IssueStatus.RESOLVED, IssueStatus.COMMUNITY_VERIFIED -> Color(0xFF10B981)
                                                IssueStatus.IN_PROGRESS, IssueStatus.UNDER_REVIEW -> Color(0xFF3B82F6)
                                                IssueStatus.REJECTED -> Color(0xFFEF4444)
                                                else -> Color(0xFFFFB74D)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(issue.description, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("University: ${issue.universityName ?: "Campus"}", color = Color.LightGray, fontSize = 12.sp)
                                        Text("Routing: ${issue.routingStatus}", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { selectedIssue = issue },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Details", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onResendNotification(issue.id)
                                                notificationMessage = "Resent email notification to University Student Leader for report ${issue.id}"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Email, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Send Email to Leader", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { issueToDelete = issue },
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
                }
            }
        }

        issueToDelete?.let { issue ->
            AlertDialog(
                onDismissRequest = { issueToDelete = null },
                title = { Text("Confirm Delete Issue Report", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to delete issue report ${issue.id}? This action cannot be undone.", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteIssue(issue.id)
                            issueToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { issueToDelete = null }) { Text("Cancel", color = Color.Gray) }
                },
                containerColor = AppColorScheme.surface
            )
        }

        // Issue Details Dialog
        selectedIssue?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedIssue = null },
                title = { Text("Issue Details — ${item.id}", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow("Report Reference", item.id)
                        DetailRow("Category", item.category.displayName)
                        DetailRow("Urgency", item.urgency)
                        DetailRow("University", item.universityName ?: "Campus")
                        DetailRow("Campus", item.campusName ?: "Main Campus")
                        DetailRow("Description", item.description)
                        DetailRow("Exact Latitude", item.issueLocation.latitude.toString())
                        DetailRow("Exact Longitude", item.issueLocation.longitude.toString())
                        DetailRow("Location Source", item.locationSource)
                        DetailRow("Reporter Name", item.reporterName ?: "Campus Student")
                        DetailRow("Reporter Email", item.reporterEmail ?: "student@campnav.org")
                        DetailRow("Reporter Phone", item.reporterPhone ?: "n/a")
                        DetailRow("Report Status", item.status.name)
                        DetailRow("Routing Status", item.routingStatus)
                        item.publicResponse?.let { DetailRow("Public Response", it) }
                        item.internalNotes?.let { DetailRow("Internal Notes", it) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedIssue = null }) {
                        Text("Close", color = AppColorScheme.primary)
                    }
                },
                containerColor = AppColorScheme.surface
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    pendingCount: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(count, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun ReviewsContent(reviews: List<CommunityReview>, onApprove: (Long) -> Unit, onReject: (Long, String) -> Unit, onDelete: (Long) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) { items(reviews) { Text(it.comment, color = Color.White) } }
}

@Composable
fun FeedbackContent(feedbacks: List<UserFeedback>, onApprove: (Long) -> Unit, onReject: (Long, String) -> Unit, onDelete: (Long) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) { items(feedbacks) { Text(it.feedbackText, color = Color.White) } }
}

@Composable
fun ContributionsContent(
    contributions: List<CommunityContribution>,
    onApprove: (Long) -> Unit,
    onReject: (Long, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedContribution by remember { mutableStateOf<CommunityContribution?>(null) }
    var rejectTargetContribution by remember { mutableStateOf<CommunityContribution?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    val filtered = when (selectedFilter) {
        "PENDING" -> contributions.filter { it.status == "PENDING" }
        "APPROVED" -> contributions.filter { it.status == "APPROVED" }
        "REJECTED" -> contributions.filter { it.status == "REJECTED" }
        else -> contributions
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Location Contributions (${contributions.size})",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "PENDING", "APPROVED", "REJECTED").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No location contributions found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.reference, fontWeight = FontWeight.Bold, color = AppColorScheme.primary, fontSize = 14.sp)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (item.areaType == "COMMERCIAL_AREA") Color(0xFFFF9800).copy(alpha = 0.2f) else Color(0xFF2196F3).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            if (item.areaType == "COMMERCIAL_AREA") "Commercial Area" else "University Area",
                                            color = if (item.areaType == "COMMERCIAL_AREA") Color(0xFFFFB74D) else Color(0xFF64B5F6),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (item.status) {
                                        "APPROVED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        "REJECTED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                        else -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        item.status,
                                        color = when (item.status) {
                                            "APPROVED" -> Color(0xFF10B981)
                                            "REJECTED" -> Color(0xFFEF4444)
                                            else -> Color(0xFFFFB74D)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(item.locationName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)

                            item.university?.let { uni ->
                                Text("University: ${uni.name}", color = Color.LightGray, fontSize = 13.sp)
                            }

                            if (item.areaType == "UNIVERSITY_AREA") {
                                val catName = item.category?.name ?: item.customCategory
                                catName?.let {
                                    Text("Category: $it", color = Color.Gray, fontSize = 12.sp)
                                }
                            }

                            Text("Contributor: ${item.contributorName} (${item.contributorContact})", color = Color.Gray, fontSize = 12.sp)
                            Text("Exact Coordinates: ${item.latitude}, ${item.longitude}", color = Color.Gray, fontSize = 12.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { selectedContribution = item },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("View Details", fontSize = 12.sp)
                                }

                                if (item.status == "PENDING") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onApprove(item.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Approve", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                rejectTargetContribution = item
                                                rejectionReasonText = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // View Details Dialog
    selectedContribution?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedContribution = null },
            title = { Text("Contribution Details — ${item.reference}", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow("Reference ID", item.reference)
                    DetailRow("Location Name", item.locationName)
                    DetailRow("Area Type", if (item.areaType == "COMMERCIAL_AREA") "Commercial Area" else "University Area")
                    item.university?.let { DetailRow("University", "${it.name} (${it.shortName ?: ""})") }
                    if (item.areaType == "UNIVERSITY_AREA") {
                        item.category?.let { DetailRow("Category", it.name) }
                        item.customCategory?.let { DetailRow("Custom Category", it) }
                    }
                    DetailRow("Exact Latitude", item.latitude.toString())
                    DetailRow("Exact Longitude", item.longitude.toString())
                    DetailRow("Description / Details", item.description ?: "None provided")
                    DetailRow("Contributor Name", item.contributorName)
                    DetailRow("Contributor Contact", item.contributorContact)
                    DetailRow("Contribution Status", item.status)
                    DetailRow("Payment Status", item.paymentStatus)
                    item.createdAt?.let { DetailRow("Submitted At", it) }
                    item.reviewedAt?.let { DetailRow("Reviewed At", it) }
                    item.reviewedBy?.let { DetailRow("Reviewed By", it) }
                    item.rejectionReason?.let { DetailRow("Rejection Reason", it) }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedContribution = null }) {
                    Text("Close", color = AppColorScheme.primary)
                }
            },
            containerColor = AppColorScheme.surface
        )
    }

    // Reject Dialog
    rejectTargetContribution?.let { item ->
        AlertDialog(
            onDismissRequest = { rejectTargetContribution = null },
            title = { Text("Reject Contribution", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter rejection reason for ${item.locationName}:", color = Color.Gray, fontSize = 13.sp)
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(item.id, rejectionReasonText.ifBlank { "Unspecified reason" })
                        rejectTargetContribution = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetContribution = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = AppColorScheme.surface
        )
    }
}

@Composable
fun LogsContent(logs: List<AdminActivityLog>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) { items(logs) { Text(it.description, color = Color.White) } }
}
