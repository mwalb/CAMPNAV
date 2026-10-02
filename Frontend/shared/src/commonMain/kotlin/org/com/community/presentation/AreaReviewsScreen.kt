package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.campus.data.CampusRepository
import org.com.campus.data.University
import org.com.community.data.CommunityRepository
import org.com.community.model.CommunityReview
import org.com.community.model.ReviewRequest
import org.com.core.ui.theme.AppColorScheme

@Composable
fun AreaReviewsScreen(
    onBack: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    var universities by remember { mutableStateOf(emptyList<University>()) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var reviews by remember { mutableStateOf(emptyList<CommunityReview>()) }

    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var contributorName by remember { mutableStateOf("") }
    var contributorContact by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val campusRepo = remember { CampusRepository() }
    val communityRepo = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            universities = campusRepo.getUniversities()
            if (universities.isNotEmpty()) {
                selectedUniversity = universities.first()
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load universities"
        }
    }

    LaunchedEffect(selectedUniversity?.id) {
        selectedUniversity?.id?.let { uniId ->
            isLoading = true
            try {
                reviews = communityRepo.getReviewsByUniversity(uniId)
            } catch (e: Exception) {
                reviews = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Community Reviews", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Read and share campus experiences", style = MaterialTheme.typography.bodySmall, color = AppColorScheme.primary)
                    }
                }
            }

            // Admin Moderation entry point under Community Review
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToAdmin),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB74D).copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin Verification & Moderation", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Authorized administrator portal for location, review & feedback moderation", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFFFB74D))
                    }
                }
            }

            item {
                Text("Select University", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    universities.forEach { uni ->
                        val isSelected = selectedUniversity?.id == uni.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedUniversity = uni },
                            label = { Text(uni.shortName ?: uni.name) }
                        )
                    }
                }
            }

            // Submit Review Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Rate This Area", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        // Star selector
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { rating = star }) {
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star stars",
                                        tint = if (star <= rating) Color(0xFFFFD700) else Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Write your review...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contributorName,
                                onValueChange = { contributorName = it },
                                label = { Text("Your Name (Optional)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            OutlinedTextField(
                                value = contributorContact,
                                onValueChange = { contributorContact = it },
                                label = { Text("Contact (Optional)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }

                        if (successMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(successMessage!!, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (selectedUniversity == null || comment.isBlank()) {
                                    errorMessage = "Please enter a review comment."
                                    return@Button
                                }
                                isSubmitting = true
                                errorMessage = null
                                successMessage = null
                                scope.launch {
                                    try {
                                        val req = ReviewRequest(
                                            universityId = selectedUniversity!!.id,
                                            locationId = null,
                                            rating = rating,
                                            comment = comment.trim(),
                                            contributorName = contributorName.ifBlank { "Anonymous" },
                                            contributorContact = contributorContact.ifBlank { null }
                                        )
                                        communityRepo.addReview(req)
                                        successMessage = "Review submitted successfully!"
                                        comment = ""
                                        reviews = communityRepo.getReviewsByUniversity(selectedUniversity!!.id)
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to submit review: ${e.message}"
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Submit Review", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Community Reviews (${reviews.size})", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColorScheme.primary)
                    }
                }
            } else if (reviews.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant)) {
                        Text("No reviews yet for this university. Be the first to review!", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }
            } else {
                items(reviews) { review ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(review.contributorName ?: "Anonymous", color = Color.White, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    (1..review.rating).forEach { _ ->
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(review.comment, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            if (!review.createdAt.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(review.createdAt!!.take(10), color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
