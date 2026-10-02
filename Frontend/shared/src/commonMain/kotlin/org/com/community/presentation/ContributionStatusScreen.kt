package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.launch
import org.com.community.data.CommunityRepository
import org.com.community.model.CommunityContribution
import org.com.core.ui.theme.AppColorScheme

@Composable
fun ContributionStatusScreen(
    initialReference: String? = null,
    onBack: () -> Unit
) {
    var referenceInput by remember { mutableStateOf(initialReference ?: "") }
    var contribution by remember { mutableStateOf<CommunityContribution?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val communityRepo = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialReference) {
        if (!initialReference.isNullOrBlank()) {
            isLoading = true
            try {
                contribution = communityRepo.getContributionByReference(initialReference)
                if (contribution == null) {
                    errorMessage = "Contribution reference not found."
                }
            } catch (e: Exception) {
                errorMessage = "Failed to fetch contribution status."
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
                    Text("Contribution Status", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Lookup Contribution", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = referenceInput,
                            onValueChange = { referenceInput = it },
                            label = { Text("Enter Reference (e.g. CAMPNAV-XXXXXX)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (referenceInput.isBlank()) {
                                    errorMessage = "Please enter a reference code."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val result = communityRepo.getContributionByReference(referenceInput.trim())
                                        if (result != null) {
                                            contribution = result
                                        } else {
                                            errorMessage = "Contribution not found for reference: $referenceInput"
                                            contribution = null
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error fetching status: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
                        ) {
                            Text("Check Status", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColorScheme.primary)
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF512A2A))) {
                        Text(errorMessage!!, color = Color(0xFFFF8A80), modifier = Modifier.padding(16.dp))
                    }
                }
            }

            if (contribution != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Reference", color = Color.Gray)
                                Text(contribution!!.reference, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Location Name", color = Color.Gray)
                                Text(contribution!!.locationName, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Area Type", color = Color.Gray)
                                Text(contribution!!.areaType, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status", color = Color.Gray)
                                Text(contribution!!.status, color = when(contribution!!.status) {
                                    "APPROVED" -> Color(0xFF4CAF50)
                                    "REJECTED" -> Color(0xFFF44336)
                                    else -> Color(0xFFFFB74D)
                                }, fontWeight = FontWeight.Bold)
                            }
                            if (contribution!!.areaType == "COMMERCIAL_AREA") {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Payment Status", color = Color.Gray)
                                    Text(contribution!!.paymentStatus, color = Color.Cyan, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!contribution!!.rejectionReason.isNullOrBlank()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rejection Reason", color = Color.Red)
                                    Text(contribution!!.rejectionReason!!, color = Color.Red)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contributor", color = Color.Gray)
                                Text(contribution!!.contributorName, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
