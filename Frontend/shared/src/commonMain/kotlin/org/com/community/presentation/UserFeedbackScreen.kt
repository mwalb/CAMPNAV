package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Send
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
import org.com.community.model.FeedbackRequest
import org.com.community.model.UserFeedback
import org.com.core.ui.theme.AppColorScheme

@Composable
fun UserFeedbackScreen(
    onBack: () -> Unit
) {
    var contributorName by remember { mutableStateOf("") }
    var contributorContact by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var approvedFeedback by remember { mutableStateOf(emptyList<UserFeedback>()) }
    var isLoadingFeedback by remember { mutableStateOf(true) }

    val repository = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    fun loadApprovedFeedback() {
        isLoadingFeedback = true
        scope.launch {
            try {
                approvedFeedback = repository.getApprovedFeedback()
            } catch (e: Exception) {
                // Ignore or handle
            } finally {
                isLoadingFeedback = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadApprovedFeedback()
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "User Feedback",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Send your suggestions, opinions & feedback to CAMPNAV",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColorScheme.primary
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Share Your Feedback",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        if (submitSuccess) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Thank you! Your feedback has been submitted successfully and is pending review.",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = contributorName,
                                onValueChange = { contributorName = it },
                                label = { Text("Your Name (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AppColorScheme.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = contributorContact,
                                onValueChange = { contributorContact = it },
                                label = { Text("Email / Phone (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AppColorScheme.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                label = { Text("Your Feedback / Suggestions *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AppColorScheme.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            if (errorMessage != null) {
                                Text(errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    if (feedbackText.isBlank()) {
                                        errorMessage = "Please enter feedback text."
                                        return@Button
                                    }
                                    isSubmitting = true
                                    errorMessage = null
                                    scope.launch {
                                        try {
                                            repository.submitFeedback(
                                                FeedbackRequest(
                                                    feedbackText = feedbackText,
                                                    contributorName = contributorName.ifBlank { null },
                                                    contributorContact = contributorContact.ifBlank { null }
                                                )
                                            )
                                            submitSuccess = true
                                            feedbackText = ""
                                            contributorName = ""
                                            contributorContact = ""
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to submit feedback: ${e.message}"
                                        } finally {
                                            isSubmitting = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary),
                                enabled = !isSubmitting
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.Send, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Submit Feedback", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "What Our Users Say",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoadingFeedback) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColorScheme.primary)
                    }
                }
            } else if (approvedFeedback.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Comment, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No verified feedback has been published yet.\nBe the first to share your experience.",
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(approvedFeedback) { fb ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "\"${fb.feedbackText}\"",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "— ${fb.contributorName ?: "Anonymous"}",
                                    color = AppColorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!fb.createdAt.isNullOrBlank()) {
                                    Text(
                                        fb.createdAt!!.take(10),
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
