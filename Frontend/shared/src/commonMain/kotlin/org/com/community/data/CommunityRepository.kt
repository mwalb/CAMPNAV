package org.com.community.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.com.community.model.*
import org.com.core.network.apiClient

class CommunityRepository {

    // --- Contributions ---
    suspend fun submitContribution(request: ContributionRequest): CommunityContribution {
        return apiClient.post("/api/contributions") {
            setBody(request)
        }.body()
    }

    suspend fun getContributionByReference(reference: String): CommunityContribution? {
        return try {
            apiClient.get("/api/contributions/$reference").body()
        } catch (e: Exception) {
            null
        }
    }

    // --- Reviews ---
    suspend fun getReviewsByUniversity(universityId: Long): List<CommunityReview> {
        return try {
            apiClient.get("/api/universities/$universityId/reviews").body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addReview(request: ReviewRequest): CommunityReview {
        return apiClient.post("/api/reviews") {
            setBody(request)
        }.body()
    }

    // --- User Feedback ---
    suspend fun submitFeedback(request: FeedbackRequest): UserFeedback {
        return apiClient.post("/api/feedback") {
            setBody(request)
        }.body()
    }

    suspend fun getApprovedFeedback(): List<UserFeedback> {
        return try {
            apiClient.get("/api/feedback/approved").body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllFeedback(token: String): List<UserFeedback> {
        return try {
            apiClient.get("/api/feedback/admin/all") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun approveFeedback(token: String, id: Long): UserFeedback {
        return apiClient.post("/api/feedback/admin/$id/approve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun rejectFeedback(token: String, id: Long, reason: String): UserFeedback {
        return apiClient.post("/api/feedback/admin/$id/reject") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(RejectRequest(reason))
        }.body()
    }

    // --- Auth & Admin ---
    suspend fun login(request: LoginRequest): LoginResponse {
        return apiClient.post("/api/auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun getAdminStats(token: String): Map<String, Any> {
        return try {
            apiClient.get("/api/admin/stats") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getAdminContributions(token: String, status: String? = null): List<CommunityContribution> {
        return try {
            apiClient.get("/api/admin/contributions") {
                header(HttpHeaders.Authorization, "Bearer $token")
                if (status != null) parameter("status", status)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun approveContribution(token: String, id: Long): CommunityContribution {
        return apiClient.post("/api/admin/contributions/$id/approve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun rejectContribution(token: String, id: Long, reason: String): CommunityContribution {
        return apiClient.post("/api/admin/contributions/$id/reject") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(RejectRequest(reason))
        }.body()
    }

    suspend fun getAdminReviews(token: String): List<CommunityReview> {
        return try {
            apiClient.get("/api/admin/reviews") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun approveReview(token: String, id: Long): CommunityReview {
        return apiClient.post("/api/admin/reviews/$id/approve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun rejectReview(token: String, id: Long, reason: String): CommunityReview {
        return apiClient.post("/api/admin/reviews/$id/reject") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(RejectRequest(reason))
        }.body()
    }

    suspend fun getAdminLogs(token: String): List<AdminActivityLog> {
        return try {
            apiClient.get("/api/admin/logs") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Issue reports (existing) ---
    suspend fun createReport(report: IssueReport): IssueReport {
        return apiClient.post("/api/community/reports") {
            setBody(report)
        }.body()
    }

    suspend fun getNearbyIssues(lat: Double, lng: Double, radius: Double = 2.0): List<IssueReport> {
        return apiClient.get("/api/community/reports/nearby") {
            parameter("lat", lat)
            parameter("lng", lng)
            parameter("radius", radius)
        }.body()
    }

    suspend fun verifyIssue(issueId: String, resolved: Boolean): IssueReport {
        return apiClient.post("/api/community/reports/$issueId/verify") {
            parameter("resolved", resolved)
        }.body()
    }

    suspend fun getIssueDetails(issueId: String): IssueReport {
        return apiClient.get("/api/community/reports/$issueId").body()
    }
}
