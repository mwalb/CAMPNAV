package org.com.community.model

import kotlinx.serialization.Serializable

@Serializable
enum class IssueCategory(val displayName: String, val iconName: String) {
    WATER("Water", "water_drop"),
    ROADS("Roads", "add_road"),
    ELECTRICITY("Electricity", "electric_bolt"),
    WASTE("Waste", "delete"),
    DRAINAGE("Drainage", "waves"),
    ENVIRONMENT("Environment", "eco"),
    PUBLIC_SAFETY("Public Safety", "security"),
    BUILDINGS("Buildings", "domain"),
    TRANSPORT("Transport", "directions_bus"),
    SANITATION("Sanitation", "clean_hands"),
    OTHER("Other", "more_horiz")
}

@Serializable
enum class IssueStatus {
    DRAFT,
    SUBMITTED,
    RECEIVED,
    UNDER_REVIEW,
    VERIFIED,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_ACTION,
    RESOLVED,
    COMMUNITY_VERIFIED,
    CLOSED,
    REJECTED,
    DUPLICATE
}

@Serializable
enum class IssuePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val street: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val region: String? = null,
    val nearbyLandmark: String? = null,
    val accuracy: Float? = null
)

@Serializable
data class IssueReport(
    val id: String,
    val category: IssueCategory,
    val subCategory: String? = null,
    val description: String,
    val voiceTranscription: String? = null,
    val issueLocation: GeoLocation,
    val reporterLocation: GeoLocation,
    val distanceToIssue: Double, // in meters
    val distanceText: String = "",
    val evidenceUris: List<String>,
    val videoUri: String? = null,
    val status: IssueStatus = IssueStatus.SUBMITTED,
    val userPriority: IssuePriority,
    val calculatedPriority: IssuePriority,
    val impactScore: Int,
    val reporterId: String,
    val timestamp: Long,
    val lastUpdated: Long,
    val assignedDepartment: String? = null,
    val assignedOfficerId: String? = null,
    val supportsCount: Int = 0,
    val verificationCount: Int = 0,
    val isEmergency: Boolean = false,
    val timeline: List<IssueTimelineEvent> = emptyList(),
    val beforeImageUri: String? = null,
    val afterImageUri: String? = null
)

@Serializable
data class IssueComment(
    val id: String,
    val issueId: String,
    val userId: String,
    val text: String,
    val timestamp: Long,
    val attachments: List<String> = emptyList(),
    val isOfficial: Boolean = false
)

@Serializable
enum class UserRole {
    CITIZEN,
    FIELD_OFFICER,
    DEPT_OFFICER,
    ADMIN
}

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: UserRole = UserRole.CITIZEN,
    val department: String? = null,
    val reputationScore: Int = 0
)

@Serializable
data class IssueTimelineEvent(
    val timestamp: Long,
    val status: IssueStatus,
    val message: String,
    val actorId: String? = null
)
