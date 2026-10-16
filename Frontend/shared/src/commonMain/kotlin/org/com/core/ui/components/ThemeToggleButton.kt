package org.com.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

val LocalIsDarkTheme = compositionLocalOf { true }
val LocalThemeToggle = compositionLocalOf { {} }

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean = LocalIsDarkTheme.current,
    onToggleTheme: () -> Unit = LocalThemeToggle.current,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggleTheme,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}
