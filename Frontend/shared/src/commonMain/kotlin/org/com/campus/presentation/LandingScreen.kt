package org.com.campus.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Park
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingScreen(
    onExploreCampuses: () -> Unit,
    onReportIssue: () -> Unit,
    onManagePlants: () -> Unit = {},
    onContentCreator: () -> Unit = {},
    onEntertainment: () -> Unit = {}
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
                        AppColorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Animated Logo/Title Area
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CAMPNAV",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 8.sp
                    )
                    Text(
                        text = "Your Ultimate Campus Guide",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Cards in responsive FlowRow
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 5
            ) {
                LandingActionCard(
                    title = "Explore\nCampuses",
                    subtitle = "Navigate universities in Tanzania",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    color = MaterialTheme.colorScheme.primary,
                    visible = startAnimation,
                    delay = 300,
                    onClick = onExploreCampuses
                )

                LandingActionCard(
                    title = "Plant & Tree\nTracker",
                    subtitle = "Manage & navigate to planted trees",
                    icon = Icons.Default.Park,
                    color = Color(0xFF4CAF50),
                    visible = startAnimation,
                    delay = 400,
                    onClick = onManagePlants
                )

                LandingActionCard(
                    title = "Content\nCreator",
                    subtitle = "Create & edit media for campus",
                    icon = Icons.Default.Movie,
                    color = Color(0xFFE91E63),
                    visible = startAnimation,
                    delay = 500,
                    onClick = onContentCreator
                )

                LandingActionCard(
                    title = "Campus\nEntertainment",
                    subtitle = "Stream live TV & IPTV channels",
                    icon = Icons.Default.LiveTv,
                    color = Color(0xFF00E5FF),
                    visible = startAnimation,
                    delay = 600,
                    onClick = onEntertainment
                )

                LandingActionCard(
                    title = "Report\nan Issue",
                    subtitle = "Share locations and help safety",
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.secondary,
                    visible = startAnimation,
                    delay = 700,
                    onClick = onReportIssue
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun LandingActionCard(
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
        animationSpec = tween(durationMillis = 800, delayMillis = delay, easing = FastOutSlowInEasing)
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = delay)
    )

    Card(
        modifier = Modifier
            .size(width = 170.dp, height = 250.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f * alpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize(),
                    tint = color
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
