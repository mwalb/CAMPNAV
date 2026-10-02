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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.campus.utils.openUri
import org.com.community.model.CommunityContribution
import org.com.core.ui.theme.AppColorScheme

@Composable
fun CommercialVerificationScreen(
    contribution: CommunityContribution,
    onBackToHub: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToHub) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Commercial Verification",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = Color(0xFFFFB74D).copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = "Pending Verification",
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Commercial Location Submitted!",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Final verification is required before your commercial location appears publicly on CAMPNAV. Please contact the owner or administrator to complete the verification process.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "SUBMISSION DETAILS",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reference Code", color = Color.Gray)
                            Text(contribution.reference, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Location Name", color = Color.Gray)
                            Text(contribution.locationName, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status", color = Color.Gray)
                            Text("PENDING / VERIFICATION", color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                        Text(
                            "OWNER / CONTACT DATA",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Owner / Contact Name", color = Color.Gray)
                            Text(contribution.contributorName, color = Color.White)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Contact Info", color = Color.Gray)
                            Text(contribution.contributorContact, color = Color.White)
                        }
                    }
                }
            }

            item {
                val contact = contribution.contributorContact.trim()
                val isPhone = contact.any { it.isDigit() } && contact.length >= 7
                val isEmail = contact.contains("@")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "CONTACT ACTIONS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    if (isPhone) {
                        val cleanPhone = contact.filter { it.isDigit() || it == '+' }
                        Button(
                            onClick = { openUri("tel:$cleanPhone") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Call Owner ($cleanPhone)", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val waPhone = cleanPhone.replace("+", "")
                                openUri("https://wa.me/$waPhone?text=Hello,%20regarding%20commercial%20location%20verification%20for%20${contribution.locationName}%20(Ref:%20${contribution.reference})")
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, null)
                            Spacer(Modifier.width(8.dp))
                            Text("WhatsApp Owner", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isEmail) {
                        Button(
                            onClick = { openUri("mailto:$contact?subject=Commercial%20Location%20Verification%20-${contribution.reference}") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Email, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Email Owner ($contact)", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Administrator Support Call
                    Button(
                        onClick = { openUri("tel:0745596995") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Contact CAMPNAV Admin (0745596995)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onBackToHub,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary)
                ) {
                    Text("Return to Community Hub", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
