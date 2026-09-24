package org.com.plant.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.com.getCurrentEpochMillis
import org.com.plant.model.*

class PlantRepository {

    private val _trees = MutableStateFlow<List<PlantedTree>>(initialTrees)
    val trees: StateFlow<List<PlantedTree>> = _trees.asStateFlow()

    fun getAllTrees(): List<PlantedTree> = _trees.value

    fun getTreeById(id: String): PlantedTree? {
        return _trees.value.find { it.id == id }
    }

    fun addTree(tree: PlantedTree) {
        val currentList = _trees.value.toMutableList()
        currentList.add(0, tree) // Newest first
        _trees.value = currentList
    }

    fun updateTree(updatedTree: PlantedTree) {
        val currentList = _trees.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedTree.id }
        if (index != -1) {
            currentList[index] = updatedTree
            _trees.value = currentList
        }
    }

    fun addCareLog(treeId: String, log: PlantCareLog) {
        val tree = getTreeById(treeId) ?: return
        val updatedLogs = tree.careLogs.toMutableList()
        updatedLogs.add(0, log)

        // If log is watering, improve soil moisture & set health
        val updatedTelemetry = if (log.careType == PlantCareType.WATERING) {
            tree.telemetry.copy(
                soilMoisturePercent = (tree.telemetry.soilMoisturePercent + 25).coerceAtMost(95),
                lastSensorSync = getCurrentEpochMillis()
            )
        } else {
            tree.telemetry
        }

        val updatedHealth = if (log.careType == PlantCareType.WATERING && tree.healthStatus == PlantHealthStatus.NEEDS_WATER) {
            PlantHealthStatus.HEALTHY
        } else {
            tree.healthStatus
        }

        val updatedTree = tree.copy(
            careLogs = updatedLogs,
            telemetry = updatedTelemetry,
            healthStatus = updatedHealth
        )
        updateTree(updatedTree)
    }

    fun updateHeight(treeId: String, newHeightCm: Double) {
        val tree = getTreeById(treeId) ?: return
        val measurementLog = PlantCareLog(
            id = "LOG-${getCurrentEpochMillis()}",
            timestamp = getCurrentEpochMillis(),
            careType = PlantCareType.MEASUREMENT,
            notes = "Height updated from ${tree.currentHeightCm} cm to $newHeightCm cm"
        )
        val updatedLogs = tree.careLogs.toMutableList().apply { add(0, measurementLog) }
        val updatedTree = tree.copy(
            currentHeightCm = newHeightCm,
            careLogs = updatedLogs
        )
        updateTree(updatedTree)
    }

    companion object {
        val initialTrees = listOf(
            PlantedTree(
                id = "PLANT-001",
                tagNumber = "TREE-2025-001",
                name = "Grand Campus Baobab",
                species = "Adansonia digitata (Baobab)",
                plantedDate = getCurrentEpochMillis() - (90L * 24 * 3600 * 1000), // 90 days ago
                latitude = -6.7824,
                longitude = 39.2083,
                address = "Main Administration Quad, UDSM Campus",
                healthStatus = PlantHealthStatus.HEALTHY,
                plantedHeightCm = 85.0,
                currentHeightCm = 145.0,
                coverPhotoUri = "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d",
                photoHistory = listOf(
                    "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d",
                    "https://images.unsplash.com/photo-1513836279014-a89f7a76ae86"
                ),
                careLogs = listOf(
                    PlantCareLog(
                        id = "LOG-101",
                        timestamp = getCurrentEpochMillis() - (2L * 24 * 3600 * 1000),
                        careType = PlantCareType.WATERING,
                        notes = "Watered 15 liters, soil nourished",
                        performedBy = "Eco Campus Volunteer"
                    ),
                    PlantCareLog(
                        id = "LOG-100",
                        timestamp = getCurrentEpochMillis() - (15L * 24 * 3600 * 1000),
                        careType = PlantCareType.FERTILIZING,
                        notes = "Organic compost blend applied to root zone",
                        performedBy = "Arborist Team"
                    )
                ),
                telemetry = PlantSensorTelemetry(
                    soilMoisturePercent = 74,
                    temperatureCelsius = 27.2,
                    sunlightHoursPerDay = 8.1,
                    foliageHealthScore = 96,
                    estimatedCanopyAreaM2 = 2.4,
                    lastSensorSync = getCurrentEpochMillis()
                ),
                notes = "Planted during Green Campus Initiative 2025. Thriving near central library lawn.",
                plantedBy = "Student Environmental Association"
            ),
            PlantedTree(
                id = "PLANT-002",
                tagNumber = "TREE-2025-002",
                name = "Science Complex Neem",
                species = "Azadirachta indica (Neem)",
                plantedDate = getCurrentEpochMillis() - (45L * 24 * 3600 * 1000),
                latitude = -6.7901,
                longitude = 39.2045,
                address = "Faculty of Science Botanical Garden",
                healthStatus = PlantHealthStatus.NEEDS_WATER,
                plantedHeightCm = 60.0,
                currentHeightCm = 98.0,
                coverPhotoUri = "https://images.unsplash.com/photo-1502082553048-f009c37129b9",
                photoHistory = listOf(
                    "https://images.unsplash.com/photo-1502082553048-f009c37129b9"
                ),
                careLogs = listOf(
                    PlantCareLog(
                        id = "LOG-201",
                        timestamp = getCurrentEpochMillis() - (5L * 24 * 3600 * 1000),
                        careType = PlantCareType.MEASUREMENT,
                        notes = "Height growth recorded at 98cm",
                        performedBy = "Botany Student"
                    )
                ),
                telemetry = PlantSensorTelemetry(
                    soilMoisturePercent = 28, // Low moisture
                    temperatureCelsius = 29.8,
                    sunlightHoursPerDay = 9.0,
                    foliageHealthScore = 81,
                    estimatedCanopyAreaM2 = 1.1,
                    lastSensorSync = getCurrentEpochMillis()
                ),
                notes = "Needs watering due to warm weather. Roots well settled.",
                plantedBy = "Botany Dept"
            ),
            PlantedTree(
                id = "PLANT-003",
                tagNumber = "TREE-2025-003",
                name = "Hostel Quad African Mahogany",
                species = "Khaya senegalensis (African Mahogany)",
                plantedDate = getCurrentEpochMillis() - (120L * 24 * 3600 * 1000),
                latitude = -6.7850,
                longitude = 39.2110,
                address = "Hall 4 Garden Greenery",
                healthStatus = PlantHealthStatus.HEALTHY,
                plantedHeightCm = 110.0,
                currentHeightCm = 195.0,
                coverPhotoUri = "https://images.unsplash.com/photo-1448375240586-882707db888b",
                photoHistory = listOf(
                    "https://images.unsplash.com/photo-1448375240586-882707db888b"
                ),
                careLogs = listOf(
                    PlantCareLog(
                        id = "LOG-301",
                        timestamp = getCurrentEpochMillis() - (10L * 24 * 3600 * 1000),
                        careType = PlantCareType.PRUNING,
                        notes = "Lower branches pruned for healthy vertical growth",
                        performedBy = "Campus Groundskeeper"
                    )
                ),
                telemetry = PlantSensorTelemetry(
                    soilMoisturePercent = 82,
                    temperatureCelsius = 26.0,
                    sunlightHoursPerDay = 7.8,
                    foliageHealthScore = 98,
                    estimatedCanopyAreaM2 = 3.2,
                    lastSensorSync = getCurrentEpochMillis()
                ),
                notes = "Strong sapling with high shade canopy potential.",
                plantedBy = "Hostel Committee"
            )
        )
    }
}
