package org.com.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.com.getPlatform

object ApiConfig {
    const val WEB_BASE_URL = "http://localhost:8080"
    const val ANDROID_BASE_URL = "http://10.0.2.2:8080"
    
    fun getBaseUrl(): String {
        val platform = getPlatform().name.lowercase()
        return if (platform.contains("android")) ANDROID_BASE_URL else WEB_BASE_URL
    }
}

val apiClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 15000
    }
    install(HttpRedirect)
    defaultRequest {
        url {
            val base = ApiConfig.getBaseUrl()
            takeFrom(base)
        }
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
}
