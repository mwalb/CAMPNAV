package org.com.campus.data

import kotlinx.serialization.Serializable

@Serializable
data class University(
    val id: Long,
    val externalId: String? = null,
    val name: String,
    val shortName: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val country: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val defaultZoom: Float? = null,
    val isActive: Boolean = true,
    val sortOrder: Int? = null,
    val campusAreaHectares: Double? = null
)

@Serializable
data class Category(
    val id: Long,
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val iconName: String? = null,
    val sortOrder: Int? = null
)

@Serializable
data class CampusLocation(
    val id: Long,
    val externalId: String? = null,
    val universityId: Long? = null,
    val campusId: Long? = null,
    val categoryId: Long? = null,
    val name: String,
    val officialName: String? = null,
    val aliases: String? = null,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val entranceLatitude: Double? = null,
    val entranceLongitude: Double? = null,
    val coordinateType: String? = null,
    val confidence: String? = null,
    val status: String? = null,
    val buildingCode: String? = null,
    val floor: String? = null,
    val roomNumber: String? = null,
    val googlePlaceId: String? = null,
    val googleName: String? = null,
    val googleAddress: String? = null,
    val googleType: String? = null,
    val verificationStatus: String? = null,
    val source: String? = null,
    val imageUrl: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val openingHours: String? = null,
    val isActive: Boolean = true,
    val sourcePrimary: String? = null,
    val sourceSecondary: String? = null,
    val sourceMap: String? = null,
    val sourceNotes: String? = null,
    val verifiedDate: String? = null
)
