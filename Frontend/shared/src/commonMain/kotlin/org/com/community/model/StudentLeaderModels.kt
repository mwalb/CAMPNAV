package org.com.community.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoutingStatus {
    UNASSIGNED,
    ASSIGNED_TO_LEADER,
    LEADER_ACKNOWLEDGED,
    REFERRED_TO_UNIVERSITY,
    AWAITING_RESPONSE,
    ROUTING_FAILED,
    CLOSED
}

@Serializable
enum class UrgencyLevel {
    LOW,
    NORMAL,
    HIGH,
    EMERGENCY
}

@Serializable
enum class LocationSource {
    CURRENT_LOCATION,
    PICK_FROM_MAP,
    MANUAL_COORDINATES
}

@Serializable
data class StudentLeaderDTO(
    val id: Long? = null,
    val leaderUserId: Long? = null,
    val leaderUsername: String,
    val leaderFullName: String,
    val leaderEmail: String,
    val leaderPhone: String? = null,
    val universityId: Long,
    val universityName: String,
    val status: String = "ACTIVE",
    val appointedByAdminUsername: String? = null,
    val appointmentDate: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val deactivationReason: String? = null
)

@Serializable
data class ReferralRequestDTO(
    val officeName: String,
    val officeContact: String? = null,
    val referralChannel: String? = "EMAIL",
    val followUpDays: Int? = 3
)

@Serializable
data class LeaderResolveRequestDTO(
    val resolutionDescription: String,
    val afterImageUrl: String? = null
)

@Serializable
data class AppointLeaderRequestDTO(
    val universityId: Long,
    val fullName: String,
    val email: String,
    val leaderUsername: String? = null
)

@Serializable
data class StatusChangeRequestDTO(
    val status: String,
    val reason: String? = null
)

@Serializable
data class EmailNotificationDTO(
    val id: Long,
    val recipientEmail: String,
    val recipientRole: String,
    val notificationType: String,
    val subject: String,
    val bodyText: String,
    val createdAt: String? = null,
    val attemptCount: Int = 0,
    val deliveryState: String = "PENDING",
    val failureReason: String? = null,
    val lastError: String? = null
)
