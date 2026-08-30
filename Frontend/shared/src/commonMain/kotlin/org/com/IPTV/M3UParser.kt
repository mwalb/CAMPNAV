package org.com.IPTV


import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.com.Channel

class M3UParser {
    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 60000
        }
        install(HttpRedirect)
    }

    suspend fun parse(playlistUrl: String): List<Channel> {
        return withContext(Dispatchers.Default) {
            try {
                val response = client.get(playlistUrl) {
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    header("Accept", "*/*")
                }

                if (response.status.isSuccess()) {
                    val channels = mutableListOf<Channel>()
                    val content = response.bodyAsText()
                    
                    var currentName = ""
                    var currentLogo: String? = null
                    var currentCategory: String? = null

                    content.lineSequence().forEach { line ->
                        val trimmedLine = line.trim()
                        when {
                            trimmedLine.startsWith("#EXTINF") -> {
                                val logoMatch = Regex("tvg-logo=\"([^\"]+)\"").find(trimmedLine)
                                currentLogo = logoMatch?.groupValues?.get(1)

                                val categoryMatch = Regex("group-title=\"([^\"]+)\"").find(trimmedLine)
                                currentCategory = categoryMatch?.groupValues?.get(1)

                                val nameIndex = trimmedLine.indexOf(",")
                                if (nameIndex != -1) {
                                    currentName = trimmedLine.substring(nameIndex + 1).trim()
                                }
                            }

                            trimmedLine.startsWith("http") || trimmedLine.startsWith("https") -> {
                                if (currentName.isNotEmpty()) {
                                    channels.add(
                                        Channel(
                                            name = currentName,
                                            url = trimmedLine,
                                            logo = currentLogo,
                                            category = currentCategory
                                        )
                                    )
                                }
                                currentName = ""
                                currentLogo = null
                                currentCategory = null
                            }
                        }
                    }
                    channels
                } else {
                    throw Exception("Failed to load playlist: ${response.status}")
                }
            } catch (e: Exception) {
                throw Exception("Error loading playlist: ${e.message}")
            }
        }
    }

    fun close() {
        client.close()
    }
}
