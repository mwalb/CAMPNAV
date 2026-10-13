package org.com.community.model

import kotlinx.serialization.Serializable

@Serializable
enum class IssueCategory(val displayName: String, val iconName: String) {
    WATER("Water and Sanitation", "water_drop"),
    ELECTRICITY("Electricity and Power", "electric_bolt"),
    ROADS("Roads and Transport", "add_road"),
    SANITATION("Sanitation and Hygiene", "clean_hands"),
    WASTE("Waste and Environment", "delete"),
    FACILITIES("Public Facilities", "domain"),
    SAFETY("Safety and Security", "security"),
    INTERNET("Communication and Internet", "wifi"),
    BUILDINGS("Buildings and Public Property", "domain"),
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
    val id: String = "",
    val universityId: Long? = null,
    val universityName: String? = null,
    val campusName: String? = null,
    val category: IssueCategory = IssueCategory.OTHER,
    val subCategory: String? = null,
    val description: String = "",
    val voiceTranscription: String? = null,
    val issueLocation: GeoLocation = GeoLocation(0.0, 0.0),
    val reporterLocation: GeoLocation = GeoLocation(0.0, 0.0),
    val distanceToIssue: Double = 0.0,
    val distanceText: String = "",
    val evidenceUris: List<String> = emptyList(),
    val videoUri: String? = null,
    val status: IssueStatus = IssueStatus.SUBMITTED,
    val routingStatus: String = "UNASSIGNED",
    val urgency: String = "NORMAL",
    val locationSource: String = "CURRENT_LOCATION",
    val userPriority: IssuePriority = IssuePriority.MEDIUM,
    val calculatedPriority: IssuePriority = IssuePriority.MEDIUM,
    val impactScore: Int = 0,
    val reporterId: String = "",
    val reporterName: String? = null,
    val reporterEmail: String? = null,
    val reporterPhone: String? = null,
    val timestamp: Long = 0,
    val lastUpdated: Long = 0,
    val assignedDepartment: String? = null,
    val assignedOfficerId: String? = null,
    val assignedLeaderName: String? = null,
    val supportsCount: Int = 0,
    val verificationCount: Int = 0,
    val isEmergency: Boolean = false,
    val timeline: List<IssueTimelineEvent> = emptyList(),
    val beforeImageUri: String? = null,
    val afterImageUri: String? = null,
    val publicResponse: String? = null,
    val internalNotes: String? = null
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
    STUDENT_LEADER,
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
    val timestamp: Long = 0,
    val status: IssueStatus = IssueStatus.SUBMITTED,
    val message: String = "",
    val actorId: String? = null
)
