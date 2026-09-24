package org.com.community.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LocationPickerMap(
    modifier: Modifier = Modifier,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    onLocationSelected: (Double, Double) -> Unit
)
