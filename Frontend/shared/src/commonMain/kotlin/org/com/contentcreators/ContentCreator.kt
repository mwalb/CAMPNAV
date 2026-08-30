package org.com.contentcreators

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImage
import kotlinx.coroutines.*
import kotlinx.serialization.*
import org.com.Channel
import org.com.PlatformVideoPlayer
import kotlin.random.Random
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.*
import kotlinx.coroutines.flow.*

// Content Creator Screen
@Composable
fun ContentCreatorScreen(
    onBack: () -> Unit,
    onSave: (MediaProject) -> Unit
) {
    var selectedMedia by remember { mutableStateOf<MediaItem?>(null) }
    var project by remember { mutableStateOf<MediaProject?>(null) }
    var activeTool by remember { mutableStateOf<EditTool?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6C63FF),
            secondary = Color(0xFF00E5FF),
            background = Color(0xFF0F0F23),
            surface = Color(0xFF1A1A3E),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Box(
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
            when {
                // Media Selection Screen
                selectedMedia == null -> MediaPickerScreen(
                    onMediaSelected = { media ->
                        selectedMedia = media
                        project = MediaProject(
                            id = Random.nextLong().toString(),
                            name = "Project ${Random.nextInt(1000, 9999)}",
                            originalMedia = media
                        )
                        isEditing = true
                    },
                    onBack = onBack
                )

                // Editing Screen
                isEditing && project != null -> EnhancedMediaEditorScreen(
                    project = project!!,
                    activeTool = activeTool,
                    onToolSelected = { activeTool = it },
                    onSave = { updatedProject ->
                        project = updatedProject
                        onSave(updatedProject)
                        onBack()
                    },
                    onCancel = {
                        isEditing = false
                        selectedMedia = null
                        project = null
                    }
                )
            }
        }
    }
}

// Media Picker Screen
@Composable
fun MediaPickerScreen(
    onMediaSelected: (MediaItem) -> Unit,
    onBack: () -> Unit
) {
    var showGallery by remember { mutableStateOf(true) }
    var showCamera by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            Text(
                "Content Creator",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Media Source Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                "Import Media",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LocalMediaPicker(
                    onMediaSelected = onMediaSelected,
                    trigger = { onClick ->
                        Button(
                            onClick = onClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload")
                        }
                    }
                )

                Button(
                    onClick = { showGallery = false; showCamera = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        
        Text(
            "Sample Gallery",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Media Grid
        if (showGallery) {
            SampleMediaGrid(
                onMediaSelected = onMediaSelected
            )
        } else {
            CameraPlaceholder(
                onMediaSelected = onMediaSelected
            )
        }
    }
}

// Sample Media Grid (Replace with actual gallery)
@Composable
fun SampleMediaGrid(
    onMediaSelected: (MediaItem) -> Unit
) {
    val sampleMedia = remember {
        listOf(
            MediaItem("1", "Sample Image 1", MediaType.IMAGE, "https://picsum.photos/400/300?random=1"),
            MediaItem("2", "Sample Image 2", MediaType.IMAGE, "https://picsum.photos/400/300?random=2"),
            MediaItem("3", "Sample Video 1", MediaType.VIDEO, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4"),
            MediaItem("4", "Sample Image 3", MediaType.IMAGE, "https://picsum.photos/400/300?random=3"),
            MediaItem("5", "Sample Video 2", MediaType.VIDEO, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_2mb.mp4"),
            MediaItem("6", "Sample Image 4", MediaType.IMAGE, "https://picsum.photos/400/300?random=4")
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sampleMedia) { media ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMediaSelected(media) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A5E)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (media.type == MediaType.IMAGE) {
                        AsyncImage(
                            model = media.uri,
                            contentDescription = media.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Video",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                            Text(
                                "Video",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    // Media type badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (media.type == MediaType.IMAGE) "IMG" else "VID",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Camera Placeholder
@Composable
fun CameraPlaceholder(
    onMediaSelected: (MediaItem) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Camera",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Camera not available in demo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onMediaSelected(
                        MediaItem(
                            id = "camera_${Random.nextLong()}",
                            name = "Camera Capture",
                            type = MediaType.IMAGE,
                            uri = "https://picsum.photos/400/300?random=${Random.nextInt(100)}"
                        )
                    )
                }
            ) {
                Text("Use Sample Image")
            }
        }
    }
}

// Enhanced Bubble Animation Composable
@Composable
fun AnimatedBubble(
    bubble: BubbleOverlay,
    isSelected: Boolean,
    containerSize: IntSize,
    onPositionChanged: (Float, Float) -> Unit,
    onSelected: () -> Unit,
    onBubbleChanged: (BubbleOverlay) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isHovered by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()

    // Enhanced animations based on bubble type
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (bubble.isAnimated) {
            when (bubble.type) {
                BubbleType.NEON_PULSE -> 1.1f
                BubbleType.GLOW -> 1.15f
                BubbleType.GLASS_MORPH -> 1.05f
                BubbleType.FIRE -> 1.2f
                else -> 1.08f
            }
        } else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 / kotlin.math.max(bubble.animationSpeed, 0.1f)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (bubble.isAnimated) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (800 / kotlin.math.max(bubble.animationSpeed, 0.1f)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = if (bubble.isAnimated) 5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1200 / kotlin.math.max(bubble.animationSpeed, 0.1f)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(bubble.x, bubble.y, containerSize) {
        if (containerSize != IntSize.Zero) {
            offsetX = bubble.x * containerSize.width
            offsetY = bubble.y * containerSize.height
        }
    }

    // Generate bubble-specific colors
    val bubbleColor = Color(bubble.color)
    val textColor = Color(bubble.textColor)
    val borderColor = Color(bubble.borderColor)

    // Gradient brush for the bubble
    val gradientBrush = if (bubble.gradientColors.isNotEmpty()) {
        Brush.linearGradient(
            colors = bubble.gradientColors.map { Color(it) },
            start = Offset(0f, 0f),
            end = Offset(1f, 1f)
        )
    } else null

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    offsetX.toInt(),
                    offsetY.toInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onSelected()
                        isHovered = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onPositionChanged(
                            (offsetX / containerSize.width).coerceIn(0f, 1f),
                            (offsetY / containerSize.height).coerceIn(0f, 1f)
                        )
                    },
                    onDragEnd = { isHovered = false }
                )
            }
            .clickable { onSelected() }
            .graphicsLayer {
                rotationZ = bubble.rotation + if (bubble.isAnimated) rotationAngle else 0f
                scaleX = bubble.scale * if (bubble.isAnimated) pulseScale else 1f
                scaleY = bubble.scale * if (bubble.isAnimated) pulseScale else 1f
                alpha = bubble.opacity
                shadowElevation = if (bubble.hasShadow) with(density) { bubble.shadowRadius.dp.toPx() } else 0f
            }
            .then(
                if (isSelected)
                    Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp))
                else Modifier
            )
    ) {
        // Background with enhanced styling
        val backgroundBrush = when {
            gradientBrush != null -> gradientBrush
            bubble.type == BubbleType.GLASS -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.15f)))
            bubble.type == BubbleType.GLASS_MORPH -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.2f)))
            bubble.type == BubbleType.GALAXY -> Brush.radialGradient(
                colors = listOf(
                    Color(bubble.color).copy(alpha = 0.8f),
                    Color(0xFF1A0033).copy(alpha = 0.9f)
                )
            )
            bubble.type == BubbleType.SUNSET -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFFFFA94D),
                    Color(0xFFFFF3E0)
                )
            )
            bubble.type == BubbleType.RAINBOW -> Brush.horizontalGradient(
                colors = listOf(
                    Color.Red.copy(alpha = 0.7f),
                    Color.Yellow.copy(alpha = 0.7f),
                    Color.Green.copy(alpha = 0.7f),
                    Color.Blue.copy(alpha = 0.7f)
                )
            )
            bubble.type == BubbleType.ICY -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE3F2FD),
                    Color(0xFFBBDEFB),
                    Color(0xFF90CAF9)
                )
            )
            bubble.type == BubbleType.FIRE -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF6B35).copy(alpha = 0.8f),
                    Color(0xFFFF4500).copy(alpha = 0.6f),
                    Color(0xFFCC3300).copy(alpha = 0.4f)
                )
            )
            bubble.type == BubbleType.CYBER_PUNK -> Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFF00FF).copy(alpha = 0.8f),
                    Color(0xFF00FFFF).copy(alpha = 0.8f)
                )
            )
            bubble.type == BubbleType.DREAMY -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFB6C1).copy(alpha = 0.8f),
                    Color(0xFFDDA0DD).copy(alpha = 0.6f),
                    Color(0xFFE6E6FA).copy(alpha = 0.4f)
                )
            )
            bubble.type == BubbleType.GOLDEN -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.8f),
                    Color(0xFFFFA500).copy(alpha = 0.6f),
                    Color(0xFFFF8C00).copy(alpha = 0.4f)
                )
            )
            bubble.type == BubbleType.AQUA -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.8f),
                    Color(0xFF00BCD4).copy(alpha = 0.6f),
                    Color(0xFF006064).copy(alpha = 0.4f)
                )
            )
            bubble.type == BubbleType.HALLOWEEN -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF6B00).copy(alpha = 0.8f),
                    Color(0xFF8B4513).copy(alpha = 0.6f),
                    Color(0xFF1A0000).copy(alpha = 0.4f)
                )
            )
            bubble.type == BubbleType.LOVE -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF4081).copy(alpha = 0.8f),
                    Color(0xFFE91E63).copy(alpha = 0.6f),
                    Color(0xFF880E4F).copy(alpha = 0.4f)
                )
            )
            bubble.type == BubbleType.VINTAGE -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF5E6D3).copy(alpha = 0.9f),
                    Color(0xFFE8D5C4).copy(alpha = 0.7f)
                )
            )
            bubble.type == BubbleType.WATERCOLOR -> Brush.radialGradient(
                colors = listOf(
                    Color(bubble.color).copy(alpha = 0.6f),
                    Color(bubble.color).copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
            else -> Brush.linearGradient(listOf(bubbleColor.copy(alpha = 0.8f), bubbleColor.copy(alpha = 0.8f)))
        }

        val bubbleShape = getBubbleShape(bubble)

        // Bubble content with all styles
        Box(
            modifier = Modifier
                .then(
                    when (bubble.type) {
                        BubbleType.GLASS, BubbleType.GLASS_MORPH ->
                            Modifier.blur(2.dp)
                        BubbleType.DREAMY ->
                            Modifier.blur(4.dp)
                        BubbleType.WATERCOLOR ->
                            Modifier.blur(3.dp)
                        else -> Modifier
                    }
                )
                .shadow(
                    elevation = when {
                        bubble.type == BubbleType.GLOW || bubble.type == BubbleType.NEON_PULSE -> 16.dp
                        bubble.type == BubbleType.NEON -> 12.dp
                        bubble.type == BubbleType.GLASS_MORPH -> 8.dp
                        bubble.hasShadow -> bubble.shadowRadius.dp
                        else -> 0.dp
                    },
                    shape = bubbleShape,
                    spotColor = when {
                        bubble.type == BubbleType.GLOW || bubble.type == BubbleType.NEON_PULSE -> bubbleColor
                        bubble.type == BubbleType.GLASS_MORPH -> Color.White.copy(alpha = 0.3f)
                        else -> Color.Transparent
                    }
                )
                .then(
                    if (bubble.hasBorder) {
                        Modifier.border(
                            width = bubble.borderWidth.dp,
                            color = borderColor,
                            shape = bubbleShape
                        )
                    } else Modifier
                )
        ) {
            // Background surface with shape
            Surface(
                color = Color.Transparent,
                shape = bubbleShape
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = backgroundBrush,
                            shape = bubbleShape
                        )
                ) {
                    // Content container
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(
                                horizontal = bubble.paddingHorizontal.dp,
                                vertical = bubble.paddingVertical.dp
                            )
                    ) {
                        // Emoji if present
                        if (bubble.emoji.isNotEmpty()) {
                            Text(
                                text = bubble.emoji,
                                fontSize = (bubble.fontSize * 1.5f).sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Main text with enhanced styling
                        Text(
                            text = bubble.text,
                            color = textColor,
                            fontSize = bubble.fontSize.sp,
                            fontWeight = if (bubble.fontWeightBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (bubble.fontStyleItalic) FontStyle.Italic else FontStyle.Normal,
                            textAlign = TextAlign.Center,
                            style = when {
                                bubble.type == BubbleType.NEON_PULSE ->
                                    MaterialTheme.typography.bodyLarge.copy(
                                        shadow = Shadow(
                                            color = bubbleColor,
                                            blurRadius = with(density) { 8.dp.toPx() }
                                        )
                                    )
                                bubble.type == BubbleType.GLOW ->
                                    MaterialTheme.typography.bodyLarge.copy(
                                        shadow = Shadow(
                                            color = bubbleColor.copy(alpha = 0.5f),
                                            blurRadius = with(density) { 12.dp.toPx() }
                                        )
                                    )
                                bubble.type == BubbleType.GLASS_MORPH ->
                                    MaterialTheme.typography.bodyLarge.copy(
                                        shadow = Shadow(
                                            color = Color.White.copy(alpha = 0.3f),
                                            blurRadius = with(density) { 4.dp.toPx() }
                                        )
                                    )
                                else -> MaterialTheme.typography.bodyMedium
                            }
                        )

                        // Star decorations
                        if (bubble.starCount > 0) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                repeat(bubble.starCount) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Glow overlay
            if (bubble.type == BubbleType.GLOW || bubble.type == BubbleType.NEON_PULSE) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    bubbleColor.copy(alpha = glowAlpha * 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = bubbleShape
                        )
                )
            }

            // Neon glow effect
            if (bubble.type == BubbleType.NEON || bubble.type == BubbleType.NEON_PULSE) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            width = 2.dp,
                            color = bubbleColor.copy(alpha = glowAlpha * 0.8f),
                            shape = bubbleShape
                        )
                        .blur(8.dp)
                )
            }
        }
    }
}

// Get bubble shape based on type
fun getBubbleShape(bubble: BubbleOverlay): Shape {
    return when (bubble.type) {
        BubbleType.ROUNDED -> RoundedCornerShape(bubble.cornerRadius.dp)
        BubbleType.SPEECH -> RoundedCornerShape(
            topStart = bubble.cornerRadius.dp,
            topEnd = bubble.cornerRadius.dp,
            bottomEnd = bubble.cornerRadius.dp,
            bottomStart = 0.dp
        )
        BubbleType.THOUGHT -> CircleShape
        BubbleType.GLOW -> RoundedCornerShape(bubble.cornerRadius.dp * 1.5f)
        BubbleType.NEON -> CutCornerShape(bubble.cornerRadius.dp)
        BubbleType.COMIC -> CutCornerShape(
            topStart = 0.dp,
            topEnd = bubble.cornerRadius.dp,
            bottomEnd = 0.dp,
            bottomStart = bubble.cornerRadius.dp
        )
        BubbleType.GLASS -> RoundedCornerShape(bubble.cornerRadius.dp * 1.2f)
        BubbleType.CLOUD -> RoundedCornerShape(50)
        BubbleType.PAINT_SPLASH -> PaintSplashShape()
        BubbleType.ANIME_POP -> AnimePopShape()
        BubbleType.CYBER_PUNK -> CyberPunkShape()
        BubbleType.DREAMY -> RoundedCornerShape(50)
        BubbleType.GOLDEN -> RoundedCornerShape(16.dp)
        BubbleType.ICY -> RoundedCornerShape(20.dp)
        BubbleType.FIRE -> RoundedCornerShape(12.dp)
        BubbleType.RAINBOW -> RoundedCornerShape(24.dp)
        BubbleType.HALLOWEEN -> RoundedCornerShape(12.dp)
        BubbleType.LOVE -> HeartShape()
        BubbleType.GALAXY -> RoundedCornerShape(30.dp)
        BubbleType.AQUA -> RoundedCornerShape(18.dp)
        BubbleType.SUNSET -> RoundedCornerShape(22.dp)
        BubbleType.NEON_PULSE -> CutCornerShape(8.dp)
        BubbleType.GLASS_MORPH -> RoundedCornerShape(28.dp)
        BubbleType.BUBBLE_POP -> BubblePopShape()
        BubbleType.STICKER -> RoundedCornerShape(8.dp)
        BubbleType.RIBBON -> RibbonShape()
        BubbleType.QUOTE -> QuoteShape()
        BubbleType.EMOTE_BUBBLE -> EmoteBubbleShape()
        BubbleType.MINIMALIST -> RoundedCornerShape(4.dp)
        BubbleType.DOTTED -> DottedShape()
        BubbleType.WAVY -> WavyShape()
        BubbleType.CRYSTAL -> CrystalShape()
        BubbleType.GLITCH -> GlitchShape()
        BubbleType.VINTAGE -> VintageShape()
        BubbleType.PAPER_CUT -> PaperCutShape()
        BubbleType.ORIGAMI -> OrigamiShape()
        BubbleType.TYPED -> TypedShape()
        BubbleType.HAND_DRAWN -> HandDrawnShape()
        BubbleType.WATERCOLOR -> WatercolorShape()
        BubbleType.OIL_PAINT -> OilPaintShape()
        BubbleType.SKETCH -> SketchShape()
        BubbleType.CARTOON -> CartoonShape()
        BubbleType.RETRO -> RetroShape()
    }
}

// Custom shapes for unique bubble types
class PaintSplashShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            quadraticTo(size.width * 0.1f, 0f, size.width * 0.3f, size.height * 0.1f)
            quadraticTo(size.width * 0.5f, -size.height * 0.1f, size.width * 0.7f, size.height * 0.15f)
            quadraticTo(size.width * 0.9f, 0f, size.width, size.height * 0.3f)
            quadraticTo(size.width * 1.1f, size.height * 0.5f, size.width * 0.9f, size.height * 0.7f)
            quadraticTo(size.width, size.height * 0.9f, size.width * 0.7f, size.height)
            quadraticTo(size.width * 0.5f, size.height * 1.1f, size.width * 0.3f, size.height * 0.9f)
            quadraticTo(size.width * 0.1f, size.height * 1.1f, 0f, size.height * 0.8f)
            quadraticTo(-size.width * 0.1f, size.height * 0.6f, 0f, size.height * 0.4f)
            close()
        }
        return Outline.Generic(path)
    }
}

class AnimePopShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            quadraticTo(size.width * 0.8f, 0f, size.width, size.height * 0.3f)
            quadraticTo(size.width, size.height * 0.7f, size.width * 0.8f, size.height)
            quadraticTo(size.width * 0.5f, size.height * 1.1f, size.width * 0.2f, size.height)
            quadraticTo(0f, size.height * 0.7f, 0f, size.height * 0.3f)
            quadraticTo(0f, 0f, size.width * 0.5f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

class CyberPunkShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.3f)
            lineTo(size.width * 0.2f, 0f)
            lineTo(size.width * 0.8f, 0f)
            lineTo(size.width, size.height * 0.3f)
            lineTo(size.width, size.height * 0.7f)
            lineTo(size.width * 0.8f, size.height)
            lineTo(size.width * 0.2f, size.height)
            lineTo(0f, size.height * 0.7f)
            close()
        }
        return Outline.Generic(path)
    }
}

class HeartShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width / 2, size.height * 0.25f)
            val dx = size.width * 0.3f
            val dy = size.height * 0.35f
            cubicTo(
                size.width / 2 + dx, size.height * 0.25f - dy,
                size.width / 2 + dx * 1.5f, size.height * 0.25f + dy * 0.5f,
                size.width / 2, size.height
            )
            cubicTo(
                size.width / 2 - dx * 1.5f, size.height * 0.25f + dy * 0.5f,
                size.width / 2 - dx, size.height * 0.25f - dy,
                size.width / 2, size.height * 0.25f
            )
        }
        return Outline.Generic(path)
    }
}

class BubblePopShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            cubicTo(
                size.width * 0.8f, -size.height * 0.1f,
                size.width, size.height * 0.2f,
                size.width * 0.9f, size.height * 0.5f
            )
            cubicTo(
                size.width, size.height * 0.8f,
                size.width * 0.8f, size.height * 1.1f,
                size.width * 0.5f, size.height
            )
            cubicTo(
                size.width * 0.2f, size.height * 1.1f,
                0f, size.height * 0.8f,
                size.width * 0.1f, size.height * 0.5f
            )
            cubicTo(
                0f, size.height * 0.2f,
                size.width * 0.2f, -size.height * 0.1f,
                size.width * 0.5f, 0f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

class RibbonShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            lineTo(size.width * 0.3f, 0f)
            lineTo(size.width * 0.7f, 0f)
            lineTo(size.width, size.height * 0.2f)
            lineTo(size.width, size.height * 0.8f)
            lineTo(size.width * 0.7f, size.height)
            lineTo(size.width * 0.5f, size.height * 0.8f)
            lineTo(size.width * 0.3f, size.height)
            lineTo(0f, size.height * 0.8f)
            close()
        }
        return Outline.Generic(path)
    }
}

class QuoteShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.1f)
            lineTo(size.width, size.height * 0.1f)
            lineTo(size.width, size.height * 0.9f)
            lineTo(0f, size.height * 0.9f)
            close()
        }
        return Outline.Generic(path)
    }
}

class EmoteBubbleShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            cubicTo(
                size.width * 0.9f, 0f,
                size.width, size.height * 0.3f,
                size.width, size.height * 0.5f
            )
            cubicTo(
                size.width, size.height * 0.7f,
                size.width * 0.9f, size.height,
                size.width * 0.5f, size.height
            )
            cubicTo(
                size.width * 0.1f, size.height,
                0f, size.height * 0.7f,
                0f, size.height * 0.5f
            )
            cubicTo(
                0f, size.height * 0.3f,
                size.width * 0.1f, 0f,
                size.width * 0.5f, 0f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

class DottedShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val dotCount = 12
            val radius = min(size.width, size.height) / 2
            val dotSize = radius / 10f
            for (i in 0 until dotCount) {
                val angle = (i * 2 * PI / dotCount).toFloat()
                val x = size.width / 2 + (radius - dotSize) * cos(angle)
                val y = size.height / 2 + (radius - dotSize) * sin(angle)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

class WavyShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val segments = 20
            val segmentWidth = size.width / segments
            moveTo(0f, size.height * 0.5f)
            for (i in 0..segments) {
                val x = i * segmentWidth
                val y = size.height * 0.5f + size.height * 0.2f * sin(i.toFloat() * 0.5f)
                lineTo(x, y)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class CrystalShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.4f)
            lineTo(size.width * 0.3f, 0f)
            lineTo(size.width * 0.7f, 0f)
            lineTo(size.width, size.height * 0.4f)
            lineTo(size.width, size.height * 0.7f)
            lineTo(size.width * 0.7f, size.height)
            lineTo(size.width * 0.3f, size.height)
            lineTo(0f, size.height * 0.7f)
            close()
        }
        return Outline.Generic(path)
    }
}

class GlitchShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val random = Random(42)
            moveTo(0f, 0f)
            var curX = 0f
            while (curX < size.width) {
                val y = random.nextFloat() * size.height * 0.3f
                val y2 = random.nextFloat() * size.height * 0.3f
                val segmentWidth = size.width * (0.05f + random.nextFloat() * 0.15f)
                lineTo(kotlin.math.min(curX + segmentWidth, size.width), y)
                lineTo(kotlin.math.min(curX + segmentWidth * 1.5f, size.width), y2)
                curX += segmentWidth * 1.5f
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class VintageShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width * 0.1f, 0f)
            lineTo(size.width * 0.9f, 0f)
            lineTo(size.width, size.height * 0.15f)
            lineTo(size.width, size.height * 0.85f)
            lineTo(size.width * 0.9f, size.height)
            lineTo(size.width * 0.1f, size.height)
            lineTo(0f, size.height * 0.85f)
            lineTo(0f, size.height * 0.15f)
            close()
        }
        return Outline.Generic(path)
    }
}

class PaperCutShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class OrigamiShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width * 0.5f, size.height * 0.7f)
            close()
        }
        return Outline.Generic(path)
    }
}

class TypedShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val cornerSize = size.width * 0.05f
            moveTo(cornerSize, 0f)
            lineTo(size.width - cornerSize, 0f)
            lineTo(size.width, cornerSize)
            lineTo(size.width, size.height - cornerSize)
            lineTo(size.width - cornerSize, size.height)
            lineTo(cornerSize, size.height)
            lineTo(0f, size.height - cornerSize)
            lineTo(0f, cornerSize)
            close()
        }
        return Outline.Generic(path)
    }
}

class HandDrawnShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val random = Random(123)
            moveTo(random.nextFloat() * size.width * 0.1f, 0f)
            var curX = 0f
            while (curX < size.width) {
                val y = random.nextFloat() * size.height * 0.15f + size.height * 0.1f
                val segmentWidth = size.width * (0.03f + random.nextFloat() * 0.06f)
                curX += segmentWidth
                lineTo(kotlin.math.min(curX, size.width), y)
            }
            lineTo(size.width, size.height)
            var curY = size.height
            while (curY > 0) {
                val xOffset = random.nextFloat() * size.width * 0.15f + size.width * 0.05f
                val segmentHeight = size.height * (0.03f + random.nextFloat() * 0.06f)
                curY -= segmentHeight
                lineTo(xOffset, kotlin.math.max(curY, 0f))
            }
            close()
        }
        return Outline.Generic(path)
    }
}

class WatercolorShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val random = Random(456)
            val points = 12
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = min(size.width, size.height) / 2

            for (i in 0..points) {
                val angle = i * 2 * PI / points
                val r = radius * (0.8f + random.nextFloat() * 0.4f)
                val x = centerX + r * cos(angle).toFloat()
                val y = centerY + r * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

class OilPaintShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val random = Random(789)
            moveTo(random.nextFloat() * size.width * 0.2f, 0f)
            var curX = 0f
            while (curX < size.width) {
                val y = size.height * (0.1f + random.nextFloat() * 0.3f)
                curX += size.width * (0.05f + random.nextFloat() * 0.1f)
                lineTo(kotlin.math.min(curX, size.width), y)
            }
            lineTo(size.width, size.height)
            var curY = size.height
            while (curY > 0) {
                val x = size.width * (0.1f + random.nextFloat() * 0.3f)
                curY -= size.height * (0.05f + random.nextFloat() * 0.1f)
                lineTo(x, kotlin.math.max(curY, 0f))
            }
            close()
        }
        return Outline.Generic(path)
    }
}

class SketchShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val random = Random(234)
            moveTo(random.nextFloat() * size.width * 0.1f, 0f)
            var curX = 0f
            while (curX < size.width) {
                val y = size.height * (0.1f + random.nextFloat() * 0.3f)
                val segmentWidth = size.width * (0.02f + random.nextFloat() * 0.04f)
                for (i in 0..2) {
                    lineTo(kotlin.math.min(curX + segmentWidth * i / 2, size.width), y + random.nextFloat() * size.height * 0.05f)
                }
                curX += segmentWidth
            }
            lineTo(size.width, size.height)
            var curY = size.height
            while (curY > 0) {
                val x = size.width * (0.05f + random.nextFloat() * 0.3f)
                curY -= size.height * (0.02f + random.nextFloat() * 0.04f)
                for (i in 0..2) {
                    lineTo(x + random.nextFloat() * size.width * 0.05f, kotlin.math.max(curY - (i * 0.01f * size.height), 0f))
                }
            }
            close()
        }
        return Outline.Generic(path)
    }
}

class CartoonShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.2f)
            cubicTo(
                size.width * 0.2f, -size.height * 0.1f,
                size.width * 0.8f, -size.height * 0.1f,
                size.width, size.height * 0.2f
            )
            cubicTo(
                size.width * 1.1f, size.height * 0.5f,
                size.width * 1.1f, size.height * 0.5f,
                size.width, size.height * 0.8f
            )
            cubicTo(
                size.width * 0.8f, size.height * 1.1f,
                size.width * 0.2f, size.height * 1.1f,
                0f, size.height * 0.8f
            )
            cubicTo(
                -size.width * 0.1f, size.height * 0.5f,
                -size.width * 0.1f, size.height * 0.5f,
                0f, size.height * 0.2f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

class RetroShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

// Enhanced Media Editor Screen with all new features
@Composable
fun EnhancedMediaEditorScreen(
    project: MediaProject,
    activeTool: EditTool?,
    onToolSelected: (EditTool?) -> Unit,
    onSave: (MediaProject) -> Unit,
    onCancel: () -> Unit
) {
    var brightness by remember { mutableStateOf(1f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var cropMode by remember { mutableStateOf(false) }
    var textOverlay by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<ColorFilter?>(null) }
    var bubbles by remember { mutableStateOf(project.bubbles) }
    var selectedBubbleId by remember { mutableStateOf<String?>(null) }
    var drawings by remember { mutableStateOf(project.drawings) }
    var shapes by remember { mutableStateOf(project.shapes) }
    var stickers by remember { mutableStateOf(project.stickers) }
    var musicOverlays by remember { mutableStateOf(project.musicOverlays) }
    var progressOverlays by remember { mutableStateOf(project.progressOverlays) }
    var counterOverlays by remember { mutableStateOf(project.counterOverlays) }
    var currentDrawingPath by remember { mutableStateOf<DrawingPath?>(null) }
    var drawingColor by remember { mutableStateOf(0xFF6C63FF) }
    var drawingThickness by remember { mutableStateOf(5f) }
    var selectedShapeId by remember { mutableStateOf<String?>(null) }
    var selectedStickerId by remember { mutableStateOf<String?>(null) }
    var textAnimation by remember { mutableStateOf<TextAnimationType?>(null) }
    var showTimeStamps by remember { mutableStateOf(false) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val editingProject = remember(
        project, brightness, contrast, saturation, rotation, cropMode, textOverlay,
        selectedFilter, bubbles, drawings, shapes, stickers, musicOverlays,
        progressOverlays, counterOverlays, textAnimation
    ) {
        project.copy(
            editHistory = listOfNotNull(
                if (brightness != 1f) EditAction(EditOperation.BRIGHTNESS, mapOf("value" to brightness.toString())) else null,
                if (contrast != 1f) EditAction(EditOperation.CONTRAST, mapOf("value" to contrast.toString())) else null,
                if (saturation != 1f) EditAction(EditOperation.SATURATION, mapOf("value" to saturation.toString())) else null,
                if (rotation != 0f) EditAction(EditOperation.ROTATE, mapOf("angle" to rotation.toString())) else null,
                if (textOverlay.isNotEmpty()) EditAction(EditOperation.TEXT_OVERLAY, mapOf("text" to textOverlay)) else null
            ),
            bubbles = bubbles,
            drawings = drawings,
            shapes = shapes,
            stickers = stickers,
            musicOverlays = musicOverlays,
            progressOverlays = progressOverlays,
            counterOverlays = counterOverlays,
            textAnimations = textAnimation?.let { mapOf(textOverlay to it) } ?: emptyMap()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F23))
    ) {
        // Top Bar
        Surface(
            color = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "Cancel", tint = Color.White)
                }

                Text(
                    project.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showTimeStamps = !showTimeStamps }) {
                        Icon(
                            Icons.Default.Timer, 
                            "Timestamps", 
                            tint = if (showTimeStamps) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                    Button(
                        onClick = { onSave(editingProject) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Export", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Preview Area (Canvas)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Shadowed Image/Video Canvas
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight(0.95f)
                    .aspectRatio(
                        if (project.originalMedia.type == MediaType.IMAGE) 1f else 16/9f,
                        matchHeightConstraintsFirst = true
                    )
                    .shadow(24.dp, RoundedCornerShape(8.dp), spotColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Media Preview
                    if (project.originalMedia.type == MediaType.IMAGE) {
                        AsyncImage(
                            model = project.originalMedia.uri,
                            contentDescription = project.originalMedia.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = 1f
                                    scaleY = 1f
                                    rotationZ = rotation
                                },
                            contentScale = ContentScale.Fit,
                            colorFilter = selectedFilter
                        )
                    } else {
                        PlatformVideoPlayer(
                            channel = Channel(
                                name = project.originalMedia.name,
                                url = project.originalMedia.uri
                            ),
                            isPlaying = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Drawing Canvas (Layered strictly on top of media)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(activeTool, drawingColor, drawingThickness) {
                                if (activeTool == EditTool.DRAW) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentDrawingPath = DrawingPath(
                                                id = Random.nextLong().toString(),
                                                points = listOf(OffsetPoint(offset.x / size.width, offset.y / size.height)),
                                                color = drawingColor,
                                                thickness = drawingThickness
                                            )
                                        },
                                        onDrag = { change, _ ->
                                            val newPoint = OffsetPoint(change.position.x / size.width, change.position.y / size.height)
                                            currentDrawingPath = currentDrawingPath?.copy(
                                                points = currentDrawingPath!!.points + newPoint
                                            )
                                        },
                                        onDragEnd = {
                                            currentDrawingPath?.let { drawings = drawings + it }
                                            currentDrawingPath = null
                                        }
                                    )
                                }
                            }
                    ) {
                        // Draw existing paths
                        drawings.forEach { path ->
                            val drawingPath = Path()
                            if (path.points.isNotEmpty()) {
                                drawingPath.moveTo(path.points[0].x * size.width, path.points[0].y * size.height)
                                path.points.forEach { p ->
                                    drawingPath.lineTo(p.x * size.width, p.y * size.height)
                                }
                            }
                            drawPath(
                                path = drawingPath,
                                color = Color(path.color).copy(alpha = path.opacity),
                                style = Stroke(width = path.thickness)
                            )
                        }

                        // Draw current path
                        currentDrawingPath?.let { path ->
                            val drawingPath = Path()
                            if (path.points.isNotEmpty()) {
                                drawingPath.moveTo(path.points[0].x * size.width, path.points[0].y * size.height)
                                path.points.forEach { p ->
                                    drawingPath.lineTo(p.x * size.width, p.y * size.height)
                                }
                            }
                            drawPath(
                                path = drawingPath,
                                color = Color(path.color),
                                style = Stroke(width = path.thickness)
                            )
                        }
                    }

                    // Overlays (Bubbles, Shapes, etc.)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { containerSize = it.size }
                    ) {
                        bubbles.forEach { bubble ->
                            AnimatedBubble(
                                bubble = bubble,
                                isSelected = selectedBubbleId == bubble.id,
                                containerSize = containerSize,
                                onPositionChanged = { x, y ->
                                    bubbles = bubbles.map { if (it.id == bubble.id) it.copy(x = x, y = y) else it }
                                },
                                onSelected = { selectedBubbleId = bubble.id },
                                onBubbleChanged = { updatedBubble ->
                                    bubbles = bubbles.map { if (it.id == bubble.id) updatedBubble else it }
                                }
                            )
                        }

                        shapes.forEach { shape ->
                            DraggableShape(
                                shape = shape,
                                isSelected = selectedShapeId == shape.id,
                                containerSize = containerSize,
                                onPositionChanged = { x: Float, y: Float ->
                                    shapes = shapes.map { if (it.id == shape.id) it.copy(x = x, y = y) else it }
                                },
                                onSelected = { selectedShapeId = shape.id }
                            )
                        }

                        stickers.forEach { sticker ->
                            DraggableSticker(
                                sticker = sticker,
                                isSelected = selectedStickerId == sticker.id,
                                containerSize = containerSize,
                                onPositionChanged = { x, y ->
                                    stickers = stickers.map { if (it.id == sticker.id) it.copy(x = x, y = y) else it }
                                },
                                onSelected = { selectedStickerId = sticker.id }
                            )
                        }
                    }
                }
            }

            // Floating Overlays (TimeStamps, Animated Text)
            if (textOverlay.isNotEmpty()) {
                AnimatedTextOverlay(
                    text = textOverlay,
                    animationType = textAnimation ?: TextAnimationType.NONE,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(Color.Black, blurRadius = 8f)
                    )
                )
            }
        }

        // Enhanced Editing Tools with more options
        EnhancedToolPanel(
            activeTool = activeTool,
            onToolSelected = onToolSelected,
            brightness = brightness,
            onBrightnessChange = { brightness = it },
            contrast = contrast,
            onContrastChange = { contrast = it },
            saturation = saturation,
            onSaturationChange = { saturation = it },
            onAddBubble = {
                val newBubble = BubbleOverlay(
                    id = Random.nextLong().toString(),
                    text = "New Bubble",
                    x = 0.5f,
                    y = 0.5f,
                    type = BubbleType.ROUNDED,
                    color = 0xFF6C63FF,
                    isAnimated = true,
                    animationSpeed = 1f,
                    fontSize = 16f
                )
                bubbles = bubbles + newBubble
                selectedBubbleId = newBubble.id
            },
            onAddSticker = {
                val emojis = listOf("🌟", "✨", "🔥", "💫", "🎨", "🌈", "⭐", "💪", "🎯", "🚀")
                val newSticker = StickerOverlay(
                    id = Random.nextLong().toString(),
                    emoji = emojis.random(),
                    x = 0.5f,
                    y = 0.5f
                )
                stickers = stickers + newSticker
                selectedStickerId = newSticker.id
            },
            onAddMusic = {
                val songs = listOf("Summer Vibes", "Chill Beat", "Electro Wave", "Acoustic Dream", "Night Drive")
                val artists = listOf("Artist", "DJ", "Band", "Solo", "Group")
                val newMusic = MusicOverlay(
                    id = Random.nextLong().toString(),
                    title = songs.random(),
                    artist = artists.random(),
                    x = 0.5f,
                    y = 0.1f
                )
                musicOverlays = musicOverlays + newMusic
            },
            onAddProgress = {
                val newProgress = ProgressOverlay(
                    id = Random.nextLong().toString(),
                    value = 0.5f,
                    x = 0.5f,
                    y = 0.9f
                )
                progressOverlays = progressOverlays + newProgress
            },
            onAddCounter = {
                val newCounter = CounterOverlay(
                    id = Random.nextLong().toString(),
                    count = 0,
                    label = "Count",
                    x = 0.5f,
                    y = 0.15f
                )
                counterOverlays = counterOverlays + newCounter
            },
            onAddTimestamp = {
                // val newTimestamp = TimeStamp(time = 0f, text = "New Moment")
                // Add to project
            },
            onAnimationChange = { animationType ->
                textAnimation = animationType
            },
            bubbles = bubbles,
            selectedBubbleId = selectedBubbleId,
            onBubbleUpdate = { updatedBubble ->
                bubbles = bubbles.map { if (it.id == updatedBubble.id) updatedBubble else it }
            },
            onBubbleDelete = { bubbleId ->
                bubbles = bubbles.filter { it.id != bubbleId }
                if (selectedBubbleId == bubbleId) selectedBubbleId = null
            },
            drawings = drawings,
            onDrawingUndo = { if (drawings.isNotEmpty()) drawings = drawings.dropLast(1) },
            drawingColor = drawingColor,
            onDrawingColorChange = { drawingColor = it },
            drawingThickness = drawingThickness,
            onDrawingThicknessChange = { drawingThickness = it },
            shapes = shapes,
            selectedShapeId = selectedShapeId,
            onShapeUpdate = { updatedShape ->
                shapes = shapes.map { if (it.id == updatedShape.id) updatedShape else it }
            },
            onShapeDelete = { shapeId ->
                shapes = shapes.filter { it.id != shapeId }
                if (selectedShapeId == shapeId) selectedShapeId = null
            }
        )
    }
}

// Enhanced Tool Panel
@Composable
fun EnhancedToolPanel(
    activeTool: EditTool?,
    onToolSelected: (EditTool?) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    onAddBubble: () -> Unit,
    onAddSticker: () -> Unit,
    onAddMusic: () -> Unit,
    onAddProgress: () -> Unit,
    onAddCounter: () -> Unit,
    onAddTimestamp: () -> Unit,
    onAnimationChange: (TextAnimationType?) -> Unit,
    bubbles: List<BubbleOverlay>,
    selectedBubbleId: String?,
    onBubbleUpdate: (BubbleOverlay) -> Unit,
    onBubbleDelete: (String) -> Unit,
    drawings: List<DrawingPath>,
    onDrawingUndo: () -> Unit,
    drawingColor: Long,
    onDrawingColorChange: (Long) -> Unit,
    drawingThickness: Float,
    onDrawingThicknessChange: (Float) -> Unit,
    shapes: List<ShapeOverlay>,
    selectedShapeId: String?,
    onShapeUpdate: (ShapeOverlay) -> Unit,
    onShapeDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A3E))
            .padding(16.dp)
    ) {
        // Tool Selection Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                EditToolButton(
                    icon = Icons.Default.Tune,
                    label = "Adjust",
                    selected = activeTool == EditTool.ADJUST,
                    onClick = { onToolSelected(if (activeTool == EditTool.ADJUST) null else EditTool.ADJUST) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.Crop,
                    label = "Crop",
                    selected = activeTool == EditTool.CROP,
                    onClick = { onToolSelected(if (activeTool == EditTool.CROP) null else EditTool.CROP) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.AutoMirrored.Filled.RotateRight,
                    label = "Rotate",
                    selected = activeTool == EditTool.ROTATE,
                    onClick = {
                        // Rotation handled by parent
                        onToolSelected(if (activeTool == EditTool.ROTATE) null else EditTool.ROTATE)
                    }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    selected = activeTool == EditTool.TEXT,
                    onClick = { onToolSelected(if (activeTool == EditTool.TEXT) null else EditTool.TEXT) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.FilterVintage,
                    label = "Filters",
                    selected = activeTool == EditTool.FILTER,
                    onClick = { onToolSelected(if (activeTool == EditTool.FILTER) null else EditTool.FILTER) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.ChatBubble,
                    label = "Bubbles",
                    selected = activeTool == EditTool.BUBBLE,
                    onClick = { onToolSelected(if (activeTool == EditTool.BUBBLE) null else EditTool.BUBBLE) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.Brush,
                    label = "Draw",
                    selected = activeTool == EditTool.DRAW,
                    onClick = { onToolSelected(if (activeTool == EditTool.DRAW) null else EditTool.DRAW) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.Category,
                    label = "Shapes",
                    selected = activeTool == EditTool.SHAPE,
                    onClick = { onToolSelected(if (activeTool == EditTool.SHAPE) null else EditTool.SHAPE) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.EmojiEmotions,
                    label = "Stickers",
                    selected = activeTool == EditTool.STICKER,
                    onClick = { onToolSelected(if (activeTool == EditTool.STICKER) null else EditTool.STICKER) }
                )
            }
            item {
                EditToolButton(
                    icon = Icons.Default.MusicNote,
                    label = "Music",
                    selected = activeTool == EditTool.MUSIC,
                    onClick = { onToolSelected(if (activeTool == EditTool.MUSIC) null else EditTool.MUSIC) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Active Tool Controls
        AnimatedVisibility(
            visible = activeTool != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column {
                when (activeTool) {
                    EditTool.BUBBLE -> {
                        EnhancedBubbleControls(
                            bubbles = bubbles,
                            selectedBubbleId = selectedBubbleId,
                            onAddBubble = onAddBubble,
                            onBubbleUpdate = onBubbleUpdate,
                            onBubbleDelete = onBubbleDelete
                        )
                    }
                    EditTool.STICKER -> {
                        StickerControls(
                            onAddSticker = onAddSticker
                        )
                    }
                    EditTool.MUSIC -> {
                        MusicControls(
                            onAddMusic = onAddMusic
                        )
                    }
                    EditTool.TEXT -> {
                        TextAnimationControls(
                            onAnimationChange = onAnimationChange
                        )
                    }
                    EditTool.DRAW -> {
                        DrawingControls(
                            drawings = drawings,
                            onUndo = onDrawingUndo,
                            drawingColor = drawingColor,
                            onColorChange = onDrawingColorChange,
                            drawingThickness = drawingThickness,
                            onThicknessChange = onDrawingThicknessChange
                        )
                    }
                    EditTool.SHAPE -> {
                        ShapeControls(
                            shapes = shapes,
                            selectedShapeId = selectedShapeId,
                            onShapeUpdate = onShapeUpdate,
                            onShapeDelete = onShapeDelete
                        )
                    }
                    EditTool.ADJUST -> {
                        AdjustmentControls(
                            brightness = brightness,
                            onBrightnessChange = onBrightnessChange,
                            contrast = contrast,
                            onContrastChange = onContrastChange,
                            saturation = saturation,
                            onSaturationChange = onSaturationChange
                        )
                    }
                    else -> {
                        Text(
                            "Select a tool to edit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Enhanced Bubble Controls
@Composable
fun EnhancedBubbleControls(
    bubbles: List<BubbleOverlay>,
    selectedBubbleId: String?,
    onAddBubble: () -> Unit,
    onBubbleUpdate: (BubbleOverlay) -> Unit,
    onBubbleDelete: (String) -> Unit
) {
    val currentBubble = bubbles.find { it.id == selectedBubbleId }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bubbles", color = Color.White, style = MaterialTheme.typography.titleSmall)
            Row {
                IconButton(onClick = onAddBubble) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                }
                if (selectedBubbleId != null) {
                    IconButton(onClick = { onBubbleDelete(selectedBubbleId) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
        }

        if (currentBubble != null) {
            Spacer(Modifier.height(8.dp))

            // Bubble Style Selection
            Text("Style", color = Color.White, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val styles = BubbleType.entries
                styles.forEach { style ->
                    item {
                        FilterChip(
                            selected = currentBubble.type == style,
                            onClick = {
                                onBubbleUpdate(currentBubble.copy(type = style))
                            },
                            label = {
                                Text(
                                    style.name.take(6),
                                    color = if (currentBubble.type == style) Color.White else Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Text Input
            OutlinedTextField(
                value = currentBubble.text,
                onValueChange = {
                    onBubbleUpdate(currentBubble.copy(text = it))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bubble Text") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(Modifier.height(8.dp))

            // Color Selection
            Text("Color", color = Color.White, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val colors = listOf(
                    0xFF6C63FF, 0xFF00E5FF, 0xFFFF6584, 0xFF4CAF50,
                    0xFFFFC107, 0xFFFFFFFF, 0xFF000000, 0xFFFF6B6B,
                    0xFF6BFF6B, 0xFF6B6BFF, 0xFFFFB6C1, 0xFFDDA0DD,
                    0xFFFFD700, 0xFFFFA500, 0xFFFF8C00, 0xFF00E5FF,
                    0xFF00BCD4, 0xFF006064, 0xFFFF4081, 0xFFE91E63
                )
                colors.forEach { colorLong ->
                    item {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (currentBubble.color == colorLong) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onBubbleUpdate(currentBubble.copy(color = colorLong))
                                }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Advanced Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Size", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = currentBubble.fontSize,
                        onValueChange = {
                            onBubbleUpdate(currentBubble.copy(fontSize = it))
                        },
                        valueRange = 8f..40f
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Opacity", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = currentBubble.opacity,
                        onValueChange = {
                            onBubbleUpdate(currentBubble.copy(opacity = it))
                        },
                        valueRange = 0.1f..1f
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rotation", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = currentBubble.rotation,
                        onValueChange = {
                            onBubbleUpdate(currentBubble.copy(rotation = it))
                        },
                        valueRange = -180f..180f
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Scale", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = currentBubble.scale,
                        onValueChange = {
                            onBubbleUpdate(currentBubble.copy(scale = it))
                        },
                        valueRange = 0.5f..2f
                    )
                }
            }

            // Animation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = currentBubble.isAnimated,
                    onCheckedChange = {
                        onBubbleUpdate(currentBubble.copy(isAnimated = it))
                    }
                )
                Text("Animate", color = Color.White, style = MaterialTheme.typography.labelSmall)

                Spacer(Modifier.weight(1f))

                if (currentBubble.isAnimated) {
                    Text("Speed", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = currentBubble.animationSpeed,
                        onValueChange = {
                            onBubbleUpdate(currentBubble.copy(animationSpeed = it))
                        },
                        valueRange = 0.5f..2f,
                        modifier = Modifier.width(100.dp)
                    )
                }
            }

            // Emoji input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Emoji", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val emojis = listOf("😀", "😍", "🔥", "🌟", "💫", "✨", "⭐", "💪", "🎯", "🚀", "🌈", "🎨")
                    emojis.forEach { emoji ->
                        item {
                            Surface(
                                onClick = { onBubbleUpdate(currentBubble.copy(emoji = if (currentBubble.emoji == emoji) "" else emoji)) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentBubble.emoji == emoji) MaterialTheme.colorScheme.primary else Color.Transparent
                            ) {
                                Text(emoji, modifier = Modifier.padding(4.dp), fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            // Additional toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = currentBubble.hasShadow,
                    onClick = { onBubbleUpdate(currentBubble.copy(hasShadow = !currentBubble.hasShadow)) },
                    label = { Text("Shadow", color = Color.White, style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = currentBubble.hasBorder,
                    onClick = { onBubbleUpdate(currentBubble.copy(hasBorder = !currentBubble.hasBorder)) },
                    label = { Text("Border", color = Color.White, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

// Sticker Controls
@Composable
fun StickerControls(
    onAddSticker: () -> Unit
) {
    Column {
        Text("Stickers", color = Color.White, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onAddSticker,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, null)
                Text("Add Sticker")
            }
        }
    }
}

// Music Controls
@Composable
fun MusicControls(
    onAddMusic: () -> Unit
) {
    Column {
        Text("Music", color = Color.White, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onAddMusic,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.MusicNote, null)
                Text("Add Music")
            }
        }
    }
}

// Text Animation Controls
@Composable
fun TextAnimationControls(
    onAnimationChange: (TextAnimationType?) -> Unit
) {
    Column {
        Text("Text Animation", color = Color.White, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val animations = TextAnimationType.entries
            animations.forEach { type ->
                item {
                    FilterChip(
                        selected = false,
                        onClick = { onAnimationChange(type) },
                        label = {
                            Text(
                                type.name.take(4),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

// Drawing Controls
@Composable
fun DrawingControls(
    drawings: List<DrawingPath>,
    onUndo: () -> Unit,
    drawingColor: Long,
    onColorChange: (Long) -> Unit,
    drawingThickness: Float,
    onThicknessChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Drawing", color = Color.White, style = MaterialTheme.typography.titleSmall)
            IconButton(
                onClick = onUndo,
                enabled = drawings.isNotEmpty()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    null,
                    tint = if (drawings.isNotEmpty()) Color.White else Color.Gray
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Text("Color", color = Color.White, style = MaterialTheme.typography.labelSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = listOf(0xFF6C63FF, 0xFF00E5FF, 0xFFFF6584, 0xFF4CAF50, 0xFFFFC107, 0xFFFFFFFF, 0xFF000000)
            colors.forEach { colorLong ->
                item {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (drawingColor == colorLong) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { onColorChange(colorLong) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Thickness", color = Color.White, style = MaterialTheme.typography.labelSmall)
            Slider(
                value = drawingThickness,
                onValueChange = onThicknessChange,
                valueRange = 1f..20f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Shape Controls
@Composable
fun ShapeControls(
    shapes: List<ShapeOverlay>,
    selectedShapeId: String?,
    onShapeUpdate: (ShapeOverlay) -> Unit,
    onShapeDelete: (String) -> Unit
) {
    val currentShape = shapes.find { it.id == selectedShapeId }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shapes", color = Color.White, style = MaterialTheme.typography.titleSmall)
            if (selectedShapeId != null) {
                IconButton(onClick = { onShapeDelete(selectedShapeId) }) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
        }

        if (currentShape != null) {
            Spacer(Modifier.height(8.dp))
            Text("Shape Color", color = Color.White, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val colors = listOf(0xFF6C63FF, 0xFF00E5FF, 0xFFFF6584, 0xFF4CAF50, 0xFFFFC107, 0xFFFFFFFF, 0xFF000000)
                colors.forEach { colorLong ->
                    item {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (currentShape.color == colorLong) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { onShapeUpdate(currentShape.copy(color = colorLong)) }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Filled", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Checkbox(
                    checked = currentShape.isFilled,
                    onCheckedChange = { onShapeUpdate(currentShape.copy(isFilled = it)) }
                )
            }
        }
    }
}

// Adjustment Controls
@Composable
fun AdjustmentControls(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Brightness", color = Color.White, style = MaterialTheme.typography.labelSmall)
        Slider(
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = 0f..2f,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text("Contrast", color = Color.White, style = MaterialTheme.typography.labelSmall)
        Slider(
            value = contrast,
            onValueChange = onContrastChange,
            valueRange = 0f..2f,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text("Saturation", color = Color.White, style = MaterialTheme.typography.labelSmall)
        Slider(
            value = saturation,
            onValueChange = onSaturationChange,
            valueRange = 0f..2f,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}

// Draggable Sticker
@Composable
fun DraggableSticker(
    sticker: StickerOverlay,
    isSelected: Boolean,
    containerSize: IntSize,
    onPositionChanged: (Float, Float) -> Unit,
    onSelected: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(sticker.x, sticker.y, containerSize) {
        if (containerSize != IntSize.Zero) {
            offsetX = sticker.x * containerSize.width
            offsetY = sticker.y * containerSize.height
        }
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(offsetX.toInt(), offsetY.toInt())
            }
            .size(
                if (containerSize.width > 0) (containerSize.width * sticker.size).dp else 50.dp
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelected() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onPositionChanged(
                            (offsetX / containerSize.width).coerceIn(0f, 1f),
                            (offsetY / containerSize.height).coerceIn(0f, 1f)
                        )
                    }
                )
            }
            .clickable { onSelected() }
            .graphicsLayer {
                rotationZ = sticker.rotation
                alpha = sticker.opacity
            }
    ) {
        Text(
            text = sticker.emoji,
            fontSize = (if (containerSize.width > 0) containerSize.width * sticker.size * 0.8f else 40f).sp,
            modifier = Modifier.fillMaxSize()
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, Color.White, RoundedCornerShape(4.dp))
            )
        }
    }
}

// Music Overlay View
@Composable
fun MusicOverlayView(
    music: MusicOverlay,
    containerSize: IntSize,
    onPlayToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (music.x * containerSize.width).toInt(),
                    (music.y * containerSize.height).toInt()
                )
            }
            .size(
                if (containerSize.width > 0) (containerSize.width * music.size).dp else 100.dp,
                if (containerSize.height > 0) (containerSize.height * 0.08f).dp else 30.dp
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onPlayToggle() }
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (music.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    music.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    music.artist,
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Progress Overlay View
@Composable
fun ProgressOverlayView(
    progress: ProgressOverlay,
    containerSize: IntSize
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (progress.x * containerSize.width).toInt() - (progress.width * containerSize.width / 2).toInt(),
                    (progress.y * containerSize.height).toInt()
                )
            }
            .size(
                (progress.width * containerSize.width).dp,
                (progress.height * containerSize.height).dp
            )
            .clip(RoundedCornerShape((progress.height * containerSize.height / 2).dp))
            .background(Color(progress.backgroundColor))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                .background(Color(progress.color))
                .clip(RoundedCornerShape((progress.height * containerSize.height / 2).dp))
        )
    }
}

// Counter Overlay View
@Composable
fun CounterOverlayView(
    counter: CounterOverlay,
    containerSize: IntSize
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (counter.x * containerSize.width).toInt(),
                    (counter.y * containerSize.height).toInt()
                )
            }
    ) {
        Text(
            text = "${counter.count} ${counter.label}",
            color = Color(counter.color),
            fontSize = counter.fontSize.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    blurRadius = with(density) { 4.dp.toPx() }
                )
            )
        )
    }
}

// Animated Text Overlay
@Composable
fun AnimatedTextOverlay(
    text: String,
    animationType: TextAnimationType,
    style: TextStyle
) {
    var animOffset by remember { mutableStateOf(0f) }
    var animAlpha by remember { mutableStateOf(1f) }
    var animScale by remember { mutableStateOf(1f) }
    var animRotation by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition()

    when (animationType) {
        TextAnimationType.FADE -> {
            val a by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animAlpha = a
        }
        TextAnimationType.BOUNCE -> {
            val s by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animScale = s
        }
        TextAnimationType.SCROLL -> {
            val o by infiniteTransition.animateFloat(
                initialValue = -200f,
                targetValue = 200f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing)
                )
            )
            animOffset = o
        }
        TextAnimationType.PULSE -> {
            val s by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animScale = s
        }
        TextAnimationType.SHAKE -> {
            val o by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animOffset = o
        }
        TextAnimationType.WAVE -> {
            val r by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animRotation = r
        }
        TextAnimationType.FLIP -> {
            val r by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing)
                )
            )
            animRotation = r
        }
        TextAnimationType.ZOOM -> {
            val s by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animScale = s
        }
        TextAnimationType.ROTATE -> {
            val r by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(5000, easing = LinearEasing)
                )
            )
            animRotation = r
        }
        TextAnimationType.GLITCH -> {
            val o by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(50, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animOffset = o
        }
        TextAnimationType.SPARKLE -> {
            val a by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            animAlpha = a
        }
        else -> {}
    }

    Text(
        text = text,
        style = style,
        modifier = Modifier
            .offset(x = animOffset.dp)
            .graphicsLayer {
                this.alpha = animAlpha
                this.scaleX = animScale
                this.scaleY = animScale
                this.rotationZ = animRotation
            }
    )
}

// Time Stamp Overlay
@Composable
fun TimeStampOverlay(
    timeStamps: List<TimeStamp>,
    containerSize: IntSize
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        timeStamps.forEach { timestamp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Text(
                    text = formatTime(timestamp.time),
                    color = Color(timestamp.color),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(50.dp)
                )
                Text(
                    text = timestamp.text,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// Helper function for time formatting
fun formatTime(time: Float): String {
    val minutes = (time / 60).toInt()
    val seconds = (time % 60).toInt()
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

// Enhanced Edit Tool enum with new tools
enum class EditTool {
    ADJUST,
    CROP,
    ROTATE,
    TEXT,
    FILTER,
    BUBBLE,
    DRAW,
    SHAPE,
    STICKER,
    MUSIC
}

@Composable
fun DraggableShape(
    shape: ShapeOverlay,
    isSelected: Boolean,
    containerSize: IntSize,
    onPositionChanged: (Float, Float) -> Unit,
    onSelected: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(shape.x, shape.y, containerSize) {
        if (containerSize != IntSize.Zero) {
            offsetX = shape.x * containerSize.width
            offsetY = shape.y * containerSize.height
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .size(
                if (containerSize.width > 0) (containerSize.width * shape.width).dp else 40.dp,
                if (containerSize.height > 0) (containerSize.height * shape.height).dp else 40.dp
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelected() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onPositionChanged(
                            (offsetX / containerSize.width).coerceIn(0f, 1f),
                            (offsetY / containerSize.height).coerceIn(0f, 1f)
                        )
                    }
                )
            }
            .clickable { onSelected() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val color = Color(shape.color).copy(alpha = shape.alpha)
            val drawStyle = if (shape.isFilled) Fill else Stroke(width = 2.dp.toPx())
            
            when (shape.type) {
                ShapeType.RECTANGLE -> drawRect(color = color, style = drawStyle)
                ShapeType.CIRCLE -> drawCircle(color = color, style = drawStyle)
                ShapeType.TRIANGLE -> {
                    val trianglePath = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path = trianglePath, color = color, style = drawStyle)
                }
                ShapeType.STAR -> {
                    val starPath = Path().apply {
                        val outerRadius = size.width / 2
                        val innerRadius = outerRadius / 2.5f
                        val center = Offset(size.width / 2, size.height / 2)
                        for (i in 0 until 10) {
                            val r = if (i % 2 == 0) outerRadius else innerRadius
                            val angle = (i * 36.0 - 90) * PI / 180.0
                            val px = center.x + cos(angle).toFloat() * r
                            val py = center.y + sin(angle).toFloat() * r
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path = starPath, color = color, style = drawStyle)
                }
                ShapeType.HEART -> {
                    val heartPath = Path().apply {
                        moveTo(size.width / 2, size.height * 0.25f)
                        val dx = size.width * 0.3f
                        val dy = size.height * 0.35f
                        cubicTo(
                            size.width / 2 + dx, size.height * 0.25f - dy,
                            size.width / 2 + dx * 1.5f, size.height * 0.25f + dy * 0.5f,
                            size.width / 2, size.height
                        )
                        cubicTo(
                            size.width / 2 - dx * 1.5f, size.height * 0.25f + dy * 0.5f,
                            size.width / 2 - dx, size.height * 0.25f - dy,
                            size.width / 2, size.height * 0.25f
                        )
                    }
                    drawPath(path = heartPath, color = color, style = drawStyle)
                }
                ShapeType.BLOB -> {
                    val blobPath = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.2f)
                        quadraticTo(size.width * 0.5f, 0f, size.width * 0.8f, size.height * 0.2f)
                        quadraticTo(size.width, size.height * 0.5f, size.width * 0.8f, size.height * 0.8f)
                        quadraticTo(size.width * 0.5f, size.height, size.width * 0.2f, size.height * 0.8f)
                        quadraticTo(0f, size.height * 0.5f, size.width * 0.2f, size.height * 0.2f)
                    }
                    drawPath(path = blobPath, color = color, style = drawStyle)
                }
                else -> {}
            }
        }
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, Color.White, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun EditToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.White
        )
    }
}

@Composable
fun FilterButton(
    name: String,
    filter: ColorFilter?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2A2A5E)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                if (filter != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.2f))
                            .graphicsLayer { colorFilter = filter }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun GlitchEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = offsetX.dp)
            .graphicsLayer {
                this.alpha = 0.3f
            }
    )
}

@Composable
fun SparkleEffect(
    intensity: Float,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = sparkleAlpha }
    )
}


