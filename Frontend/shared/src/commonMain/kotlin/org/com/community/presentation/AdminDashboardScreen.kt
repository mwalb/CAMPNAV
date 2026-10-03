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
import org.com.community.model.AdminActivityLog
import org.com.community.model.CommunityContribution
import org.com.community.model.CommunityReview
import org.com.community.model.UserFeedback
import org.com.core.ui.theme.AppColorScheme

enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
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

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var rejectTargetId by remember { mutableStateOf<Long?>(null) }
    var rejectFeedbackTargetId by remember { mutableStateOf<Long?>(null) }
    var rejectReviewTargetId by remember { mutableStateOf<Long?>(null) }
    var deleteReviewTargetId by remember { mutableStateOf<Long?>(null) }
    var deleteFeedbackTargetId by remember { mutableStateOf<Long?>(null) }
    var detailContributionTarget by remember { mutableStateOf<CommunityContribution?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

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
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        val isDesktop = maxWidth >= 768.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColorScheme.surfaceVariant.copy(alpha = 0.9f),
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
                            Icon(Icons.Default.Menu, "Toggle Navigation", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "ADMIN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = selectedSection.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Main View Container
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Desktop Sidebar
                    if (isDesktop) {
                        Surface(
                            modifier = Modifier
                                .width(260.dp)
                                .fillMaxHeight(),
                            color = AppColorScheme.surfaceVariant.copy(alpha = 0.5f),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "SECTIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                                )

                                AdminSection.entries.forEach { section ->
                                    val isSelected = selectedSection == section
                                    val badgeCount = when (section) {
                                        AdminSection.LOCATION_CONTRIBUTIONS -> contributions.count { it.status == "PENDING" }
                                        AdminSection.COMMUNITY_REVIEWS -> reviews.count { it.status == "PENDING" }
                                        AdminSection.USER_FEEDBACK -> feedbacks.count { it.status == "PENDING" }
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

                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                                SidebarNavItem(
                                    title = "Logout",
                                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                                    isSelected = false,
                                    onClick = onLogout
                                )
                            }
                        }
                    }

                    // Content Pane
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp)
                    ) {
                        if (isLoading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AppColorScheme.primary)
                            }
                        } else {
                            when (selectedSection) {
                                AdminSection.OVERVIEW -> OverviewContent(contributions, reviews, feedbacks, logs) { section -> selectedSection = section }
                                AdminSection.COMMUNITY_REVIEWS -> ReviewsContent(
                                    reviews = reviews,
                                    onApprove = { id -> scope.launch { communityRepo.approveReview(token, id); loadData() } },
                                    onReject = { id -> rejectReviewTargetId = id },
                                    onDelete = { id -> deleteReviewTargetId = id }
                                )
                                AdminSection.USER_FEEDBACK -> FeedbackContent(
                                    feedbacks = feedbacks,
                                    onApprove = { id -> scope.launch { communityRepo.approveFeedback(token, id); loadData() } },
                                    onReject = { id -> rejectFeedbackTargetId = id },
                                    onDelete = { id -> deleteFeedbackTargetId = id }
                                )
                                AdminSection.LOCATION_CONTRIBUTIONS -> ContributionsContent(
                                    contributions = contributions,
                                    onApprove = { id -> scope.launch { communityRepo.approveContribution(token, id); loadData() } },
                                    onReject = { id -> rejectTargetId = id },
                                    onViewDetails = { item -> detailContributionTarget = item }
                                )
                                AdminSection.ACTIVITY_LOGS -> LogsContent(logs)
                            }
                        }
                    }
                }

                // Mobile Drawer / Overlay Menu
                if (!isDesktop && isMobileMenuOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { isMobileMenuOpen = false }
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight(),
                            color = AppColorScheme.surfaceVariant,
                            shadowElevation = 16.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ADMIN MENU", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { isMobileMenuOpen = false }) {
                                        Icon(Icons.Default.Close, null, tint = Color.White)
                                    }
                                }

                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                                AdminSection.entries.forEach { section ->
                                    val isSelected = selectedSection == section
                                    val badgeCount = when (section) {
                                        AdminSection.LOCATION_CONTRIBUTIONS -> contributions.count { it.status == "PENDING" }
                                        AdminSection.COMMUNITY_REVIEWS -> reviews.count { it.status == "PENDING" }
                                        AdminSection.USER_FEEDBACK -> feedbacks.count { it.status == "PENDING" }
                                        else -> 0
                                    }

                                    SidebarNavItem(
                                        title = section.title,
                                        icon = section.icon,
                                        isSelected = isSelected,
                                        badgeCount = badgeCount,
                                        onClick = {
                                            selectedSection = section
                                            isMobileMenuOpen = false
                                        }
                                    )
                                }

                                Spacer(Modifier.weight(1f))

                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                                SidebarNavItem(
                                    title = "Logout",
                                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                                    isSelected = false,
                                    onClick = {
                                        isMobileMenuOpen = false
                                        onLogout()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Location Contribution Details Dialog
        if (detailContributionTarget != null) {
            val item = detailContributionTarget!!
            AlertDialog(
                onDismissRequest = { detailContributionTarget = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contribution Details", fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { detailContributionTarget = null }) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow("Reference", item.reference)
                        DetailRow("Location Name", item.locationName)
                        DetailRow("University", item.university?.name ?: "N/A")
                        DetailRow("Area Type", item.areaType)
                        DetailRow("Category", item.category?.name ?: "N/A")
                        DetailRow("Description", item.description ?: "None provided")
                        DetailRow("Latitude", item.latitude.toString())
                        DetailRow("Longitude", item.longitude.toString())
                        DetailRow("Contributor Name", item.contributorName)
                        DetailRow("Contributor Contact", item.contributorContact)
                        DetailRow("Contribution Status", item.status)
                        DetailRow("Payment Status", item.paymentStatus)
                        if (!item.rejectionReason.isNullOrBlank()) {
                            DetailRow("Rejection Reason", item.rejectionReason)
                        }
                        DetailRow("Submitted Date", item.createdAt ?: "N/A")
                        if (!item.reviewedAt.isNullOrBlank()) {
                            DetailRow("Reviewed Date", item.reviewedAt)
                            DetailRow("Reviewed By", item.reviewedBy ?: "Admin")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { detailContributionTarget = null },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
                    ) {
                        Text("Close")
                    }
                },
                containerColor = AppColorScheme.surfaceVariant
            )
        }

        // Rejection Dialog for Contributions
        if (rejectTargetId != null) {
            AlertDialog(
                onDismissRequest = { rejectTargetId = null },
                title = { Text("Reject Contribution") },
                text = {
                    Column {
                        Text("Please provide a reason for rejection:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            label = { Text("Rejection Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = rejectTargetId!!
                            scope.launch {
                                communityRepo.rejectContribution(token, id, rejectionReason.ifBlank { "Does not meet guidelines" })
                                rejectTargetId = null
                                rejectionReason = ""
                                loadData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Confirm Reject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectTargetId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rejection Dialog for Feedback
        if (rejectFeedbackTargetId != null) {
            AlertDialog(
                onDismissRequest = { rejectFeedbackTargetId = null },
                title = { Text("Reject User Feedback") },
                text = {
                    Column {
                        Text("Please provide a reason for rejection:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            label = { Text("Rejection Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = rejectFeedbackTargetId!!
                            scope.launch {
                                communityRepo.rejectFeedback(token, id, rejectionReason.ifBlank { "Inappropriate or irrelevant feedback" })
                                rejectFeedbackTargetId = null
                                rejectionReason = ""
                                loadData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Confirm Reject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectFeedbackTargetId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rejection Dialog for Reviews
        if (rejectReviewTargetId != null) {
            AlertDialog(
                onDismissRequest = { rejectReviewTargetId = null },
                title = { Text("Reject Community Review") },
                text = {
                    Column {
                        Text("Please provide a reason for rejection:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            label = { Text("Rejection Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = rejectReviewTargetId!!
                            scope.launch {
                                communityRepo.rejectReview(token, id, rejectionReason.ifBlank { "Inappropriate content" })
                                rejectReviewTargetId = null
                                rejectionReason = ""
                                loadData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Confirm Reject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectReviewTargetId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Review Confirmation Dialog
        if (deleteReviewTargetId != null) {
            AlertDialog(
                onDismissRequest = { deleteReviewTargetId = null },
                title = { Text("Delete Community Review") },
                text = { Text("Are you sure you want to delete this review? This action cannot be undone and will permanently delete the review from PostgreSQL.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = deleteReviewTargetId!!
                            scope.launch {
                                communityRepo.deleteReview(token, id)
                                deleteReviewTargetId = null
                                loadData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteReviewTargetId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Feedback Confirmation Dialog
        if (deleteFeedbackTargetId != null) {
            AlertDialog(
                onDismissRequest = { deleteFeedbackTargetId = null },
                title = { Text("Delete User Feedback") },
                text = { Text("Are you sure you want to delete this user feedback? This action cannot be undone and will permanently delete the feedback from PostgreSQL.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = deleteFeedbackTargetId!!
                            scope.launch {
                                communityRepo.deleteFeedback(token, id)
                                deleteFeedbackTargetId = null
                                loadData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Confirm Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteFeedbackTargetId = null }) {
                        Text("Cancel")
                    }
                }
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) AppColorScheme.primary else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                color = if (isSelected) Color.White else Color.LightGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (badgeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color.White else Color(0xFFFFB74D)
                ) {
                    Text(
                        badgeCount.toString(),
                        color = if (isSelected) AppColorScheme.primary else Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewContent(
    contributions: List<CommunityContribution>,
    reviews: List<CommunityReview>,
    feedbacks: List<UserFeedback>,
    logs: List<AdminActivityLog>,
    onNavigateSection: (AdminSection) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Dashboard Overview", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Summary of community submissions & moderation status", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Reviews",
                    count = reviews.size.toString(),
                    pendingCount = reviews.count { it.status == "PENDING" },
                    icon = Icons.Default.RateReview,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(AdminSection.COMMUNITY_REVIEWS) }
                )
                MetricCard(
                    title = "Feedback",
                    count = feedbacks.size.toString(),
                    pendingCount = feedbacks.count { it.status == "PENDING" },
                    icon = Icons.Default.Feedback,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(AdminSection.USER_FEEDBACK) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Contributions",
                    count = contributions.size.toString(),
                    pendingCount = contributions.count { it.status == "PENDING" },
                    icon = Icons.Default.AddLocation,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(AdminSection.LOCATION_CONTRIBUTIONS) }
                )
                MetricCard(
                    title = "Activity Logs",
                    count = logs.size.toString(),
                    pendingCount = 0,
                    icon = Icons.Default.History,
                    color = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateSection(AdminSection.ACTIVITY_LOGS) }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }

        items(logs.take(5)) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = AppColorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.description, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Text("${log.action} • ${log.actorUsername}", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
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
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                if (pendingCount > 0) {
                    Surface(color = Color(0xFFFFB74D).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text("$pendingCount pending", color = Color(0xFFFFB74D), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(count, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun ReviewsContent(
    reviews: List<CommunityReview>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Community Reviews (${reviews.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (reviews.isEmpty()) {
            item { Text("No reviews found.", color = Color.Gray) }
        } else {
            items(reviews) { review ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(review.contributorName ?: "Contributor", color = Color.White, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${review.rating} ⭐", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                Text(review.status, color = when(review.status) {
                                    "APPROVED" -> Color(0xFF4CAF50)
                                    "REJECTED" -> Color(0xFFF44336)
                                    else -> Color(0xFFFFB74D)
                                }, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(review.comment, color = Color.LightGray)

                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (review.status == "PENDING") {
                                Button(
                                    onClick = { onApprove(review.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Approve")
                                }
                                Button(
                                    onClick = { onReject(review.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                ) {
                                    Text("Reject")
                                }
                            }
                            Button(
                                onClick = { onDelete(review.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackContent(
    feedbacks: List<UserFeedback>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("User Feedback (${feedbacks.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (feedbacks.isEmpty()) {
            item { Text("No user feedback found.", color = Color.Gray) }
        } else {
            items(feedbacks) { fb ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(fb.contributorName ?: "Contributor", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(fb.status, color = when(fb.status) {
                                "APPROVED" -> Color(0xFF4CAF50)
                                "REJECTED" -> Color(0xFFF44336)
                                else -> Color(0xFFFFB74D)
                            }, fontWeight = FontWeight.Bold)
                        }
                        if (!fb.contributorContact.isNullOrBlank()) {
                            Text("Contact: ${fb.contributorContact}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text("Feedback: \"${fb.feedbackText}\"", color = Color.LightGray, fontSize = 14.sp)

                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (fb.status == "PENDING") {
                                Button(
                                    onClick = { onApprove(fb.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Approve")
                                }
                                Button(
                                    onClick = { onReject(fb.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                ) {
                                    Text("Reject")
                                }
                            }
                            Button(
                                onClick = { onDelete(fb.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContributionsContent(
    contributions: List<CommunityContribution>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onViewDetails: (CommunityContribution) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Location Contributions (${contributions.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (contributions.isEmpty()) {
            item { Text("No location contributions found.", color = Color.Gray) }
        } else {
            items(contributions) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(item.locationName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when(item.status) {
                                    "APPROVED" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    "REJECTED" -> Color(0xFFF44336).copy(alpha = 0.2f)
                                    else -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    item.status,
                                    color = when(item.status) {
                                        "APPROVED" -> Color(0xFF4CAF50)
                                        "REJECTED" -> Color(0xFFF44336)
                                        else -> Color(0xFFFFB74D)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text("University: ${item.university?.name ?: "N/A"}", color = AppColorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Ref: ${item.reference} | Area Type: ${item.areaType}", color = Color.Gray, fontSize = 12.sp)

                        // Mandatory Latitude and Longitude Display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Latitude: ${item.latitude}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Longitude: ${item.longitude}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Text("Submitted by: ${item.contributorName} (${item.contributorContact})", color = Color.LightGray, fontSize = 12.sp)
                        if (!item.description.isNullOrBlank()) {
                            Text("Desc: ${item.description}", color = Color.LightGray, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onViewDetails(item) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColorScheme.primary)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "View Details", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("View Details", fontSize = 12.sp)
                            }

                            if (item.status == "PENDING") {
                                Button(
                                    onClick = { onApprove(item.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Approve", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onReject(item.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
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

@Composable
fun LogsContent(logs: List<AdminActivityLog>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Activity Logs (${logs.size})", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (logs.isEmpty()) {
            item { Text("No activity logs found.", color = Color.Gray) }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action, color = AppColorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(log.timestamp ?: "", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(log.description, color = Color.White)
                        Text("Actor: ${log.actorUsername}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
