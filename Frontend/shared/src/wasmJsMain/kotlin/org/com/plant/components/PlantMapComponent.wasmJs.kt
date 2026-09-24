package org.com.plant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.plant.model.PlantedTree

@Composable
actual fun PlantMapComponent(
    modifier: Modifier,
    trees: List<PlantedTree>,
    selectedTree: PlantedTree?,
    onTreeSelected: (PlantedTree) -> Unit,
    isSatelliteMode: Boolean,
    onNavigateToTree: (PlantedTree) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B2A1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Park,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Interactive Plant Map",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = if (isSatelliteMode) "Satellite Mode Active (${trees.size} Trees)" else "Standard Map Active (${trees.size} Trees)",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        selectedTree?.let { tree ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Park, contentDescription = null, tint = Color(0xFF4CAF50))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tree.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${tree.species} • ${tree.currentHeightCm}cm", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(onClick = { onNavigateToTree(tree) }) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("NAVIGATE", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
