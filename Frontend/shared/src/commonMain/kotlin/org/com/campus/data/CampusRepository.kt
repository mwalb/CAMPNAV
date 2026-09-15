package org.com.campus.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import org.com.core.network.apiClient

class CampusRepository {
    private val client = apiClient

    suspend fun getUniversities(): List<University> {
        return client.get("/api/universities").body()
    }

    suspend fun getUniversity(id: Long): University {
        return client.get("/api/universities/$id").body()
    }

    suspend fun getLocations(universityId: Long, categoryIds: List<Long>? = null): List<CampusLocation> {
        return client.get("/api/universities/${universityId}/locations") {
            if (categoryIds != null && categoryIds.isNotEmpty()) {
                parameter("categoryIds", categoryIds.joinToString(","))
            }
        }.body()
    }

    suspend fun getCategories(universityId: Long? = null): List<Category> {
        val path = if (universityId != null) "/api/universities/$universityId/categories" else "/api/categories"
        return client.get(path).body()
    }

    suspend fun searchLocations(universityId: Long, query: String, categoryIds: List<Long>? = null): List<CampusLocation> {
        return client.get("/api/universities/${universityId}/search") {
            parameter("query", query)
            if (categoryIds != null && categoryIds.isNotEmpty()) {
                parameter("categoryIds", categoryIds.joinToString(","))
            }
        }.body()
    }
}
