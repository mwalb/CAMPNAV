package org.com.community.model

import kotlinx.serialization.Serializable
import org.com.campus.data.Category
import org.com.campus.data.University
import org.com.campus.data.CampusLocation

@Serializable
data class CommunityContribution(
    val id: Long,
    val reference: String,
    val university: University? = null,
    val locationName: String,
    val areaType: String, // 'UNIVERSITY_AREA' or 'COMMERCIAL_AREA'
    val category: Category? = null,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val contributorName: String,
    val contributorContact: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val paymentStatus: String = "PAYMENT_NOT_REQUIRED", // PAYMENT_NOT_REQUIRED, PAYMENT_PENDING, PAYMENT_PAID, PAYMENT_FAILED, PAYMENT_REFUNDED
    val rejectionReason: String? = null,
    val createdAt: String? = null,
    val reviewedAt: String? = null,
    val reviewedBy: String? = null
)

@Serializable
data class CommunityReview(
    val id: Long,
    val university: University? = null,
    val location: CampusLocation? = null,
    val rating: Int,
    val comment: String,
    val contributorName: String? = null,
    val contributorContact: String? = null,
    val status: String = "APPROVED",
    val createdAt: String? = null
)

@Serializable
data class UserFeedback(
    val id: Long,
    val feedbackText: String,
    val contributorName: String? = null,
    val contributorContact: String? = null,
    val status: String = "PENDING",
    val rejectionReason: String? = null,
    val createdAt: String? = null
)

@Serializable
data class AdminActivityLog(
    val id: Long,
    val action: String,
    val actorUsername: String,
    val description: String,
    val relatedEntityId: String? = null,
    val ipAddress: String? = null,
    val timestamp: String? = null
)

@Serializable
data class ContributionRequest(
    val universityId: Long,
    val locationName: String,
    val areaType: String,
    val categoryId: Long? = null,
    val customCategory: String? = null,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val contributorName: String,
    val contributorContact: String
)

@Serializable
data class ReviewRequest(
    val universityId: Long,
    val locationId: Long?,
    val rating: Int,
    val comment: String,
    val contributorName: String?,
    val contributorContact: String?
)

@Serializable
data class FeedbackRequest(
    val feedbackText: String,
    val contributorName: String?,
    val contributorContact: String?
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val username: String
)

@Serializable
data class RejectRequest(
    val reason: String
)
