package org.com.campus.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.com.campus.data.CampusLocation
import org.com.campus.data.University

@Composable
expect fun CampusMap(
    modifier: Modifier = Modifier,
    university: University,
    locations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit = {}
)
