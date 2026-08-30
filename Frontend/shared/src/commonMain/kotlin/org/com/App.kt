package org.com

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil3.compose.AsyncImage
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.com.IPTV.M3UParser
import org.com.contentcreators.ContentCreatorScreen
import kotlin.random.Random

// Data Models
@Serializable
data class Channel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val category: String? = null
)

data class PlayerState(
    val isBuffering: Boolean = false,
    val error: String? = null
)

enum class AppMode {
    Selection,
    Entertainment,
    ContentCreator
}

// Main App Entry Point
@Composable
fun MainApp() {
    var mode by remember { mutableStateOf(AppMode.Selection) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6C63FF),
            secondary = Color(0xFF00E5FF),
            tertiary = Color(0xFFFF6584),
            background = Color(0xFF0F0F23),
            surface = Color(0xFF1A1A3E),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF2A2A5E),
            onSurfaceVariant = Color.Gray
        ),
        typography = Typography(
            displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            bodyLarge = TextStyle(fontSize = 16.sp),
            bodyMedium = TextStyle(fontSize = 14.sp),
            bodySmall = TextStyle(fontSize = 12.sp)
        )
    ) {
        when (mode) {
            AppMode.Selection -> SelectionScreen(
                onModeSelected = { mode = it }
            )
            AppMode.Entertainment -> IPTVPlayerApp(
                onBackToSelection = { mode = AppMode.Selection }
            )
            AppMode.ContentCreator -> ContentCreatorScreen(
                onBack = { mode = AppMode.Selection },
                onSave = { /* Handle project save */ }
            )
        }
    }
}

@Composable
fun SelectionScreen(onModeSelected: (AppMode) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0F23), Color(0xFF1A1A3E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Welcome to CampNav",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Choose your experience",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            SelectionCard(
                title = "Content Creator",
                description = "Edit photos and videos with powerful tools",
                icon = Icons.Default.Edit,
                onClick = { onModeSelected(AppMode.ContentCreator) }
            )

            SelectionCard(
                title = "Entertainment",
                description = "Watch live TV channels and streams",
                icon = Icons.Default.LiveTv,
                onClick = { onModeSelected(AppMode.Entertainment) }
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A5E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.width(20.dp))
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// IPTV App Screen
@Composable
fun IPTVPlayerApp(onBackToSelection: () -> Unit) {
    var selectedScreen by remember { mutableStateOf(Screen.Home) }
    var playlistUrl by remember { mutableStateOf("https://iptv-org.github.io/iptv/index.m3u") }
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedScreen) {
            Screen.Home -> HomeScreen(
                playlistUrl = playlistUrl,
                onPlaylistUrlChange = { playlistUrl = it },
                onLoadPlaylist = {
                    isLoading = true
                    error = null
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            val loadedChannels = M3UParser().parse(playlistUrl)
                            withContext(Dispatchers.Main) {
                                channels = loadedChannels
                                selectedScreen = Screen.Channels
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                error = "${e::class.simpleName}: ${e.message}"
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        }
                    }
                },
                isLoading = isLoading,
                error = error,
                onBack = onBackToSelection
            )

            Screen.Channels -> ChannelsScreen(
                channels = channels,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onChannelClick = {
                    selectedChannel = it
                    isPlaying = true
                },
                onBack = { selectedScreen = Screen.Home }
            )
        }

        // Video Player Overlay
        if (selectedChannel != null) {
            FullscreenVideoPlayer(
                channel = selectedChannel!!,
                isPlaying = isPlaying,
                onIsPlayingChange = { isPlaying = it },
                onDismiss = {
                    selectedChannel = null
                    isPlaying = false
                }
            )
        }
    }
}

enum class Screen {
    Home,
    Channels
}

// Home Screen with Animated Background
@Composable
fun HomeScreen(
    playlistUrl: String,
    onPlaylistUrlChange: (String) -> Unit,
    onLoadPlaylist: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F23),
                        Color(0xFF1A1A3E),
                        Color(0xFF0F0F23)
                    )
                )
            )
    ) {
        // Animated floating particles
        AnimatedParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header with Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
            
            // Animated Logo
            AnimatedLogo()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "IPTV Player",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.shadow(10.dp, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stream your favorite channels anywhere",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Playlist URL Input
            OutlinedTextField(
                value = playlistUrl,
                onValueChange = onPlaylistUrlChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Enter M3U playlist URL...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = "URL",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Load Button with animation
            Button(
                onClick = onLoadPlaylist,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Loading...")
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Load Playlist"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Load Channels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error Message
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// Animated Logo with Gradient
@Composable
fun AnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .rotate(rotation)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6C63FF),
                        Color(0xFF00E5FF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.LiveTv,
            contentDescription = "IPTV Logo",
            modifier = Modifier.size(60.dp),
            tint = Color.White
        )
    }
}

// Animated Floating Particles
@Composable
fun AnimatedParticles() {
    val particles = remember {
        List(15) {
            Particle(
                x = Random.nextFloat() * 1000,
                size = Random.nextFloat() * 20 + 5,
                duration = Random.nextInt(4000, 8000),
                delay = Random.nextInt(0, 3000),
                xOffset = Random.nextFloat() * 200 - 100,
                color = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat()
                )
            )
        }
    }

    particles.forEach { particle ->
        val infiniteTransition = rememberInfiniteTransition()
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = particle.duration,
                    delayMillis = particle.delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        val offsetX by infiniteTransition.animateFloat(
            initialValue = particle.x,
            targetValue = particle.x + particle.xOffset,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = particle.duration,
                    delayMillis = particle.delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        Box(
            modifier = Modifier
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(particle.size.dp)
                .clip(CircleShape)
                .background(
                    particle.color.copy(alpha = 0.3f)
                )
        )
    }
}

data class Particle(
    val x: Float,
    val size: Float,
    val duration: Int,
    val delay: Int,
    val xOffset: Float,
    val color: Color
)

// Channels Screen with Search
@Composable
fun ChannelsScreen(
    channels: List<Channel>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onBack: () -> Unit
) {
    val filteredChannels = remember(channels, searchQuery) {
        if (searchQuery.isEmpty()) {
            channels
        } else {
            channels.filter { channel ->
                channel.name.contains(searchQuery, ignoreCase = true) ||
                        channel.category?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F23),
                        Color(0xFF1A1A3E)
                    )
                )
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Channels",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${filteredChannels.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            placeholder = { Text("Search channels...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color.Gray
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Channels Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredChannels, key = { it.url }) { channel ->
                ChannelCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) }
                )
            }

            if (filteredChannels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = "No results",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No channels found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Text(
                                text = "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Channel Card with Hover Animation
@Composable
fun ChannelCard(
    channel: Channel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A3E).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2A2A5E), Color(0xFF1A1A3E))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Channel Logo
                if (channel.logo != null) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = channel.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                if (channel.category != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = channel.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Fullscreen Video Player
@Composable
fun FullscreenVideoPlayer(
    channel: Channel,
    isPlaying: Boolean,
    onIsPlayingChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var playerState by remember { mutableStateOf(PlayerState()) }
    val controlsAlpha = remember { mutableStateOf(1f) }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
            controlsAlpha.value = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
                controlsAlpha.value = if (showControls) 1f else 0f
            }
    ) {
        // ACTUAL VIDEO PLAYER - Platform-specific
        PlatformVideoPlayer(
            channel = channel,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize(),
            onStateChanged = { playerState = it }
        )

        // Buffering Indicator
        if (playerState.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Error Message
        if (playerState.error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = playerState.error ?: "Playback Error",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .alpha(controlsAlpha.value)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = channel.category ?: "Live Stream",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Live Indicator
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Buffering/Progress Indicator
                        if (playerState.isBuffering) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onIsPlayingChange(!isPlaying) }
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
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

// Platform-specific video player
@Composable
expect fun PlatformVideoPlayer(
    channel: Channel,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onStateChanged: (PlayerState) -> Unit = {}
)
