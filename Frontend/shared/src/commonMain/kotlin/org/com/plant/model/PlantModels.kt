package org.com.plant.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlantHealthStatus(val displayName: String, val description: String) {
    HEALTHY("Healthy", "Optimal growth & vibrant foliage"),
    NEEDS_WATER("Needs Water", "Soil moisture below threshold"),
    NEEDS_FERTILIZER("Needs Fertilizer", "Nutrient boost recommended"),
    CRITICAL("Critical Care", "Attention needed immediately"),
    RECOVERING("Recovering", "Post-treatment monitoring")
}

@Serializable
enum class PlantCareType(val displayName: String, val iconName: String) {
    WATERING("Watering", "water_drop"),
    FERTILIZING("Fertilizing", "eco"),
    PRUNING("Pruning", "content_cut"),
    MEASUREMENT("Growth Check", "straighten"),
    PHOTO_UPDATE("Photo Snapshot", "photo_camera"),
    REMOTE_INSPECTION("Remote Virtual Check", "satellite_alt")
}

@Serializable
data class PlantCareLog(
    val id: String,
    val timestamp: Long,
    val careType: PlantCareType,
    val notes: String,
    val photoUri: String? = null,
    val performedBy: String = "Plant Manager"
)

@Serializable
data class PlantSensorTelemetry(
    val soilMoisturePercent: Int, // e.g., 68%
    val temperatureCelsius: Double, // e.g., 26.5°C
    val sunlightHoursPerDay: Double, // e.g., 7.5 hrs
    val foliageHealthScore: Int, // 0 - 100
    val estimatedCanopyAreaM2: Double, // e.g., 1.8 m²
    val lastSensorSync: Long
)

@Serializable
data class PlantedTree(
    val id: String,
    val tagNumber: String,
    val name: String,
    val species: String,
    val plantedDate: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val healthStatus: PlantHealthStatus = PlantHealthStatus.HEALTHY,
    val plantedHeightCm: Double,
    val currentHeightCm: Double,
    val coverPhotoUri: String? = null,
    val photoHistory: List<String> = emptyList(),
    val careLogs: List<PlantCareLog> = emptyList(),
    val telemetry: PlantSensorTelemetry,
    val notes: String = "",
    val plantedBy: String = "Campus Eco-Team"
)
