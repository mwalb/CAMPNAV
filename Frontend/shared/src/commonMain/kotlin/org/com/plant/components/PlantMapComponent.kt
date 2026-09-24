package org.com.plant.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.com.plant.model.PlantedTree

@Composable
expect fun PlantMapComponent(
    modifier: Modifier = Modifier,
    trees: List<PlantedTree>,
    selectedTree: PlantedTree? = null,
    onTreeSelected: (PlantedTree) -> Unit = {},
    isSatelliteMode: Boolean = false,
    onNavigateToTree: (PlantedTree) -> Unit = {}
)
