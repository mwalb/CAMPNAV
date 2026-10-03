package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.com.core.ui.theme.AppColorScheme

@Composable
fun CommunityHubScreen(
    onBack: () -> Unit,
    onNavigateToAddLocation: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToContact: () -> Unit,
    onNavigateToAdmin: () -> Unit = {}
) {
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
            // Header
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
                        text = "Community Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explore reviews, share feedback & suggest locations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CommunityCard(
                        title = "Community Reviews",
                        subtitle = "Read and share experiences about university areas",
                        icon = Icons.Default.RateReview,
                        color = Color(0xFF00E5FF),
                        onClick = onNavigateToReviews
                    )
                }

                item {
                    CommunityCard(
                        title = "User Feedback",
                        subtitle = "Send your suggestions, opinions and feedback to CAMPNAV",
                        icon = Icons.Default.Feedback,
                        color = Color(0xFF9C27B0),
                        onClick = onNavigateToFeedback
                    )
                }

                item {
                    CommunityCard(
                        title = "Add New Location",
                        subtitle = "Suggest a new university or commercial location",
                        icon = Icons.Default.AddLocation,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToAddLocation
                    )
                }

                item {
                    CommunityCard(
                        title = "Contact Us",
                        subtitle = "Get in touch with CAMPNAV support (0745596995)",
                        icon = Icons.Default.Phone,
                        color = Color(0xFF4CAF50),
                        onClick = onNavigateToContact
                    )
                }

                item {
                    CommunityCard(
                        title = "ADMIN",
                        subtitle = "Authorized administrator access",
                        icon = Icons.Default.AdminPanelSettings,
                        color = Color(0xFFFFB74D),
                        onClick = onNavigateToAdmin
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxSize(),
                    tint = color
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
