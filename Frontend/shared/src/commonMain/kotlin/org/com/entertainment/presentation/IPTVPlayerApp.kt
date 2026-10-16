package org.com.entertainment.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import org.com.entertainment.data.M3UParser
import org.com.entertainment.model.Channel
import org.com.entertainment.model.PlayerState

@Composable
fun IPTVPlayerApp(onBackToSelection: () -> Unit) {
    var channels by remember { mutableStateOf(M3UParser.defaultHighStrengthChannels) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    val defaultPlaylists = listOf(
        "https://iptv-org.github.io/iptv/index.m3u",
        "https://iptv-org.github.io/iptv/languages/swa.m3u",
        "https://raw.githubusercontent.com/iptv-org/iptv/master/streams/tz.m3u"
    )

    fun loadGlobalStreams() {
        isLoading = true
        error = null
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val loadedChannels = M3UParser().parseWithFallbacks(defaultPlaylists)
                withContext(Dispatchers.Main) {
                    if (loadedChannels.isNotEmpty()) {
                        channels = loadedChannels
                    }
                }
            } catch (e: Exception) {
                // Default high-strength channels remain available
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGlobalStreams()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ChannelsScreen(
            channels = channels,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedCategory = selectedCategoryFilter,
            onCategorySelect = { selectedCategoryFilter = it },
            onChannelClick = { channel ->
                selectedChannel = channel
                isPlaying = true
            },
            onBack = onBackToSelection,
            onReload = { loadGlobalStreams() }
        )

        // Fullscreen Video Player Overlay
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

// Channels Selection Screen with Search & Categories
@Composable
fun ChannelsScreen(
    channels: List<Channel>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onBack: () -> Unit,
    onReload: () -> Unit
) {
    val categoryListState = rememberLazyListState()
    val channelsGridState = rememberLazyGridState()

    val categories = remember(channels) {
        channels.mapNotNull { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filteredChannels = remember(channels, searchQuery, selectedCategory) {
        channels.filter { channel ->
            val matchesQuery = searchQuery.isEmpty() ||
                    channel.name.contains(searchQuery, ignoreCase = true) ||
                    channel.category?.contains(searchQuery, ignoreCase = true) == true
            val matchesCategory = selectedCategory == null || channel.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Campus Entertainment",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReload) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

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
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            placeholder = { Text("Search videos & channels...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // Categories Layout (Horizontally Scrollable Row)
        if (categories.isNotEmpty()) {
            LazyRow(
                state = categoryListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All Categories") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onCategorySelect(if (selectedCategory == cat) null else cat) },
                        label = { Text(cat, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Channels Grid
        LazyVerticalGrid(
            state = channelsGridState,
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredChannels, key = { channel -> "${channel.name}-${channel.url}" }) { channel ->
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Try a different search term or reload",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Channel Card
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = channel.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                if (channel.category != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
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
    PlatformBackHandler(enabled = true, onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ACTUAL VIDEO PLAYER - Platform-specific
        PlatformVideoPlayer(
            channel = channel,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize(),
            onStateChanged = { }
        )

        // Top-left Back-navigation button ONLY
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to selection",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Back",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
