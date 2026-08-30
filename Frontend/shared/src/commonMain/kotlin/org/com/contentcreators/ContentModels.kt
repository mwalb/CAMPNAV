package org.com.contentcreators

import kotlinx.serialization.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Serializable
data class MediaItem(
    val id: String,
    val name: String,
    val type: MediaType,
    val uri: String,
    val thumbnail: String? = null,
    val createdAt: Long = 0L
)

@Serializable
enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO
}

@Serializable
enum class EditOperation {
    FILTER,
    CROP,
    ROTATE,
    BRIGHTNESS,
    CONTRAST,
    SATURATION,
    TEXT_OVERLAY,
    STICKER,
    TRIM,
    BUBBLE,
    DRAW,
    SHAPE
}

@Serializable
data class EditAction(
    val operation: EditOperation,
    val params: Map<String, String> = emptyMap()
)

@Serializable
enum class BubbleType {
    ROUNDED,
    SPEECH,
    THOUGHT,
    GLOW,
    NEON,
    COMIC,
    GLASS,
    CLOUD,
    PAINT_SPLASH,
    ANIME_POP,
    CYBER_PUNK,
    DREAMY,
    GOLDEN,
    ICY,
    FIRE,
    RAINBOW,
    HALLOWEEN,
    LOVE,
    GALAXY,
    AQUA,
    SUNSET,
    NEON_PULSE,
    GLASS_MORPH,
    BUBBLE_POP,
    STICKER,
    RIBBON,
    QUOTE,
    EMOTE_BUBBLE,
    MINIMALIST,
    DOTTED,
    WAVY,
    CRYSTAL,
    GLITCH,
    VINTAGE,
    PAPER_CUT,
    ORIGAMI,
    TYPED,
    HAND_DRAWN,
    WATERCOLOR,
    OIL_PAINT,
    SKETCH,
    CARTOON,
    RETRO
}

@Serializable
enum class StrokeStyle {
    Solid, Dashed, Dotted
}

@Serializable
data class BubbleOverlay(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val type: BubbleType,
    val color: Long,
    val fontSize: Float = 16f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val textColor: Long = 0xFFFFFFFF,
    val shadowRadius: Float = 4f,
    val borderWidth: Float = 0f,
    val borderColor: Long = 0x00000000,
    val cornerRadius: Float = 16f,
    val paddingHorizontal: Float = 16f,
    val paddingVertical: Float = 8f,
    val glowIntensity: Float = 0f,
    val animationSpeed: Float = 1f,
    val waveAmplitude: Float = 0f,
    val emoji: String = "",
    // We cannot serialize FontStyle/FontWeight directly easily without custom serializers
    // but for now let's keep them as they were in the snippet if possible or use primitives
    val fontStyleItalic: Boolean = false,
    val fontWeightBold: Boolean = true,
    val hasShadow: Boolean = true,
    val hasBorder: Boolean = false,
    val isAnimated: Boolean = false,
    val pulseSpeed: Float = 1f,
    val gradientColors: List<Long> = emptyList(),
    val strokeStyle: StrokeStyle = StrokeStyle.Solid,
    val starCount: Int = 0,
    val sparkleIntensity: Float = 0f,
    val blurAmount: Float = 0f,
    val isFlipped: Boolean = false,
    // val textAlignment: TextAlign = TextAlign.Center, // Needs serializer
    val fontFamily: String = "Default"
)

@Serializable
data class OffsetPoint(val x: Float, val y: Float)

@Serializable
enum class BrushType {
    SOLID, GRADIENT, NEON, GLOW, RAINBOW, FIRE, ICE
}

@Serializable
data class DrawingPath(
    val id: String,
    val points: List<OffsetPoint>,
    val color: Long,
    val thickness: Float,
    val opacity: Float = 1f,
    val style: StrokeStyle = StrokeStyle.Solid,
    val brushType: BrushType = BrushType.SOLID,
    val gradientColors: List<Long> = emptyList()
)

@Serializable
enum class ShapeType {
    RECTANGLE, CIRCLE, TRIANGLE, STAR, HEART, BLOB,
    DIAMOND, PENTAGON, HEXAGON, OCTAGON, CROSS, ARROW,
    INFINITY, CLOVER, MOON, SUN, CLOUD_SHAPE, LIGHTNING,
    FLOWER, LEAF, DROP, SPIRAL, GEAR, EXPLOSION
}

@Serializable
data class ShapeOverlay(
    val id: String,
    val type: ShapeType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Long,
    val alpha: Float = 1f,
    val isFilled: Boolean = true
)

@Serializable
data class StickerOverlay(
    val id: String,
    val emoji: String,
    val x: Float,
    val y: Float,
    val size: Float = 0.15f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
)

@Serializable
data class MusicOverlay(
    val id: String,
    val title: String,
    val artist: String,
    val x: Float,
    val y: Float,
    val size: Float = 0.1f,
    val isPlaying: Boolean = false
)

@Serializable
data class ProgressOverlay(
    val id: String,
    val value: Float,
    val x: Float,
    val y: Float,
    val width: Float = 0.5f,
    val height: Float = 0.04f,
    val color: Long = 0xFF6C63FF,
    val backgroundColor: Long = 0x33FFFFFF
)

@Serializable
data class CounterOverlay(
    val id: String,
    val count: Int,
    val label: String,
    val x: Float,
    val y: Float,
    val fontSize: Float = 24f,
    val color: Long = 0xFFFFFFFF
)

@Serializable
enum class TextAnimationType {
    NONE, FADE, BOUNCE, SCROLL, TYPING, PULSE, SHAKE, WAVE, FLIP, ZOOM, ROTATE, GLITCH, RAINBOW, SPARKLE
}

@Serializable
data class TimeStamp(
    val time: Float,
    val text: String,
    val color: Long = 0xFFFFFFFF
)

@Serializable
data class MediaProject(
    val id: String,
    val name: String,
    val originalMedia: MediaItem,
    val editHistory: List<EditAction> = emptyList(),
    val bubbles: List<BubbleOverlay> = emptyList(),
    val drawings: List<DrawingPath> = emptyList(),
    val shapes: List<ShapeOverlay> = emptyList(),
    val stickers: List<StickerOverlay> = emptyList(),
    val musicOverlays: List<MusicOverlay> = emptyList(),
    val progressOverlays: List<ProgressOverlay> = emptyList(),
    val counterOverlays: List<CounterOverlay> = emptyList(),
    val textAnimations: Map<String, TextAnimationType> = emptyMap(),
    val timeStamps: List<TimeStamp> = emptyList(),
    val previewUri: String? = null,
    val lastModified: Long = 0L
)
