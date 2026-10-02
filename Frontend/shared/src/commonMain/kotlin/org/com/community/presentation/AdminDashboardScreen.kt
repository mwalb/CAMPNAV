package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun AdminDashboardScreen(
    token: String,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Contributions, 1: Reviews, 2: Feedback, 3: Logs
    var contributions by remember { mutableStateOf(emptyList<CommunityContribution>()) }
    var reviews by remember { mutableStateOf(emptyList<CommunityReview>()) }
    var feedbacks by remember { mutableStateOf(emptyList<UserFeedback>()) }
    var logs by remember { mutableStateOf(emptyList<AdminActivityLog>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var rejectTargetId by remember { mutableStateOf<Long?>(null) }
    var rejectFeedbackTargetId by remember { mutableStateOf<Long?>(null) }
    var rejectReviewTargetId by remember { mutableStateOf<Long?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    val communityRepo = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    fun loadData() {
        isLoading = true
        scope.launch {
            try {
                contributions = communityRepo.getAdminContributions(token)
                reviews = communityRepo.getAdminReviews(token)
                feedbacks = communityRepo.getAllFeedback(token)
                logs = communityRepo.getAdminLogs(token)
            } catch (e: Exception) {
                errorMessage = "Failed to load admin data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(token) {
        loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Admin Moderation Portal", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Authorized Administrator Verification & Moderation", style = MaterialTheme.typography.bodySmall, color = AppColorScheme.primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TabButton(title = "Contribs (${contributions.size})", isSelected = selectedTab == 0) { selectedTab = 0 }
                TabButton(title = "Reviews (${reviews.size})", isSelected = selectedTab == 1) { selectedTab = 1 }
                TabButton(title = "Feedback (${feedbacks.size})", isSelected = selectedTab == 2) { selectedTab = 2 }
                TabButton(title = "Logs (${logs.size})", isSelected = selectedTab == 3) { selectedTab = 3 }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        if (contributions.isEmpty()) {
                            item { Text("No contributions found.", color = Color.Gray) }
                        } else {
                            items(contributions) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(item.locationName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text(item.status, color = when(item.status) {
                                                "APPROVED" -> Color(0xFF4CAF50)
                                                "REJECTED" -> Color(0xFFF44336)
                                                else -> Color(0xFFFFB74D)
                                            }, fontWeight = FontWeight.Bold)
                                        }
                                        Text("Ref: ${item.reference} | Type: ${item.areaType}", color = Color.Gray, fontSize = 12.sp)
                                        Text("Submitted by: ${item.contributorName} (${item.contributorContact})", color = Color.LightGray, fontSize = 12.sp)
                                        if (!item.description.isNullOrBlank()) {
                                            Text("Desc: ${item.description}", color = Color.LightGray, fontSize = 13.sp)
                                        }

                                        if (item.status == "PENDING") {
                                            Spacer(Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            communityRepo.approveContribution(token, item.id)
                                                            loadData()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                                ) {
                                                    Text("Approve")
                                                }
                                                Button(
                                                    onClick = { rejectTargetId = item.id },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                                                ) {
                                                    Text("Reject")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedTab == 1) {
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
                                            Text(review.contributorName ?: "Anonymous", color = Color.White, fontWeight = FontWeight.Bold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Rating: ${review.rating} ⭐", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                                Text(review.status, color = when(review.status) {
                                                    "APPROVED" -> Color(0xFF4CAF50)
                                                    "REJECTED" -> Color(0xFFF44336)
                                                    else -> Color(0xFFFFB74D)
                                                }, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(review.comment, color = Color.LightGray)

                                        if (review.status == "PENDING") {
                                            Spacer(Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            communityRepo.approveReview(token, review.id)
                                                            loadData()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                                ) {
                                                    Text("Approve")
                                                }
                                                Button(
                                                    onClick = { rejectReviewTargetId = review.id },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                                                ) {
                                                    Text("Reject")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedTab == 2) {
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
                                            Text(fb.contributorName ?: "Anonymous", color = Color.White, fontWeight = FontWeight.Bold)
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
                                        if (!fb.createdAt.isNullOrBlank()) {
                                            Text("Date: ${fb.createdAt.take(19)}", color = Color.Gray, fontSize = 11.sp)
                                        }

                                        if (fb.status == "PENDING") {
                                            Spacer(Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            communityRepo.approveFeedback(token, fb.id)
                                                            loadData()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                                ) {
                                                    Text("Verify / Approve")
                                                }
                                                Button(
                                                    onClick = { rejectFeedbackTargetId = fb.id },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                                                ) {
                                                    Text("Reject")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
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
            }
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
    }
}

@Composable
fun TabButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AppColorScheme.primary else AppColorScheme.surfaceVariant
        )
    ) {
        Text(title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
