package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.campus.utils.openUri
import org.com.core.ui.components.CenteredPageContainer
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        CenteredPageContainer(
            maxWidth = 660.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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
                            text = "Contact Us",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Get in touch with CAMPNAV support & administration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColorScheme.primary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 660.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = AppColorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = AppColorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Contact CAMPNAV",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Have a question, suggestion, or need assistance navigating campus? Reach out to our support team directly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                        ContactInfoCard(
                            icon = Icons.Default.Email,
                            title = "Email Support",
                            value = "charlesnestory793@gmail.com",
                            actionText = "Email",
                            onClick = {
                                try {
                                    openUri("mailto:charlesnestory793@gmail.com")
                                } catch (e: Exception) {
                                    println("[CAMPNAV] Failed to open email: ${e.message}")
                                }
                            }
                        )

                        ContactInfoCard(
                            icon = Icons.Default.Phone,
                            title = "Phone Support",
                            value = "0748409358",
                            actionText = "Call",
                            onClick = {
                                try {
                                    openUri("tel:0748409358")
                                } catch (e: Exception) {
                                    println("[CAMPNAV] Failed to open phone: ${e.message}")
                                }
                            }
                        )

                        ContactInfoCard(
                            icon = Icons.Default.Email,
                            title = "Email Support",
                            value = "raphaelfrank720@gmail.com",
                            actionText = "Email",
                            onClick = {
                                try {
                                    openUri("mailto:raphaelfrank720@gmail.com")
                                } catch (e: Exception) {
                                    println("[CAMPNAV] Failed to open email: ${e.message}")
                                }
                            }
                        )

                        ContactInfoCard(
                            icon = Icons.Default.Phone,
                            title = "Phone Support",
                            value = "0745596995",
                            actionText = "Call",
                            onClick = {
                                try {
                                    openUri("tel:0745596995")
                                } catch (e: Exception) {
                                    println("[CAMPNAV] Failed to open phone: ${e.message}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    actionText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = AppColorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(2.dp))
                    Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(actionText, color = AppColorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
