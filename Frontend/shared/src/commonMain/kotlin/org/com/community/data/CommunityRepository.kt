package org.com.community.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.com.community.model.*
import org.com.core.network.apiClient

class CommunityRepository {
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
