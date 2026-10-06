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

private fun extractEmail(contact: String): String? {
    val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    return emailRegex.find(contact)?.value
}

private fun extractPhone(contact: String): String? {
    val email = extractEmail(contact)
    val textWithoutEmail = if (email != null) contact.replace(email, "") else contact
    val phoneRegex = Regex("\\+?[0-9][0-9\\s\\-]{6,}[0-9]")
    val match = phoneRegex.find(textWithoutEmail)?.value?.trim()
    return if (!match.isNullOrBlank()) match else null
}

private fun normalizeWhatsAppPhone(phone: String): String {
    val digitsOnly = phone.filter { it.isDigit() }
    return when {
        digitsOnly.startsWith("0") && digitsOnly.length == 10 -> "255" + digitsOnly.substring(1)
        digitsOnly.startsWith("255") -> digitsOnly
        else -> digitsOnly
    }
}

@Composable
fun CommercialVerificationScreen(
    contribution: CommunityContribution,
    onBackToHub: () -> Unit
) {
    val rawContact = contribution.contributorContact.trim()
    val extractedEmail = extractEmail(rawContact)
    val extractedPhone = extractPhone(rawContact) ?: if (extractedEmail == null && rawContact.any { it.isDigit() }) rawContact else null

    val displayPhone = extractedPhone ?: if (rawContact.any { it.isDigit() }) rawContact else if (rawContact.isNotBlank() && !rawContact.contains("@")) rawContact else "Not provided"
    val displayEmail = extractedEmail ?: if (rawContact.contains("@")) rawContact else "Not provided"

    val cleanPhone = displayPhone.filter { it.isDigit() || it == '+' }.ifBlank { rawContact.filter { it.isDigit() || it == '+' } }
    val waPhone = normalizeWhatsAppPhone(cleanPhone.ifBlank { rawContact })
    val mailTarget = if (displayEmail != "Not provided") displayEmail else rawContact

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
                            "OWNER DETAILS",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Owner Name", color = Color.Gray, fontSize = 12.sp)
                            Text(contribution.contributorName, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Phone", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                displayPhone,
                                color = if (displayPhone != "Not provided") Color.White else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Email", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                displayEmail,
                                color = if (displayEmail != "Not provided") Color.White else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
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

                    // 1. Call Owner
                    Button(
                        onClick = {
                            val phone = cleanPhone.ifBlank { "0745596995" }
                            openUri("tel:$phone")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Call Owner", fontWeight = FontWeight.Bold)
                    }

                    // 2. WhatsApp
                    Button(
                        onClick = {
                            val wa = waPhone.ifBlank { "255745596995" }
                            openUri("https://wa.me/$wa?text=Hello,%20regarding%20commercial%20location%20verification%20for%20${contribution.locationName}%20(Ref:%20${contribution.reference})")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, null)
                        Spacer(Modifier.width(8.dp))
                        Text("WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // 3. Email Us
                    Button(
                        onClick = {
                            val mail = if (mailTarget.isNotBlank() && mailTarget != "Not provided") mailTarget else "info@campnav.com"
                            openUri("mailto:$mail?subject=Commercial%20Location%20Verification%20-${contribution.reference}")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Email Us", fontWeight = FontWeight.Bold, color = Color.Black)
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
