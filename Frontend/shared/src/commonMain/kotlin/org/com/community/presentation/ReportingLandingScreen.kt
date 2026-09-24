package org.com.community.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingLandingScreen(
    onReportNew: () -> Unit,
    onViewDashboard: () -> Unit,
    onBack: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColorScheme.background,
                        Color(0xFF1A1A3E)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "COMMUNITY REPORTING",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Help us improve our community",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    ReportingActionCard(
                        title = "Report\nNew Issue",
                        subtitle = "Snap a photo and share location",
                        icon = Icons.Default.Assignment,
                        color = MaterialTheme.colorScheme.primary,
                        visible = startAnimation,
                        delay = 200,
                        onClick = onReportNew
                    )

                    ReportingActionCard(
                        title = "Community\nDashboard",
                        subtitle = "View and support existing reports",
                        icon = Icons.Default.Dashboard,
                        color = MaterialTheme.colorScheme.secondary,
                        visible = startAnimation,
                        delay = 400,
                        onClick = onViewDashboard
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // SOS Card
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(800, 600)) + expandVertically(tween(800, 600))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { /* Emergency Flow */ },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4444).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFF4444).copy(alpha = 0.2f)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF4444), modifier = Modifier.padding(12.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("SAFETY SOS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                                Text("Emergency reporting for critical issues", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportingActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    visible: Boolean,
    delay: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(600, delayMillis = delay)
    )

    Card(
        modifier = Modifier
            .size(width = 180.dp, height = 240.dp)
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(subtitle, color = Color.Gray, fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
    }
}
