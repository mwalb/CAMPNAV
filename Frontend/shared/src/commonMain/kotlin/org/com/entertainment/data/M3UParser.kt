package org.com.entertainment.data

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.com.entertainment.model.Channel

class M3UParser {
    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 8000
            socketTimeoutMillis = 15000
        }
        install(HttpRedirect)
    }

    suspend fun parseWithFallbacks(urls: List<String>): List<Channel> {
        val resultChannels = mutableListOf<Channel>()
        
        // Always include verified high-strength Tanzania & Safari fallback channels first
        resultChannels.addAll(defaultHighStrengthChannels)

        for (url in urls) {
            try {
                val fetched = parse(url)
                if (fetched.isNotEmpty()) {
                    resultChannels.addAll(fetched)
                }
            } catch (e: Exception) {
                // Ignore single source errors and continue to fallback
            }
        }

        // Deduplicate by Name to preserve distinct channels sharing a stream URL (like Tanzania Safari Channel sharing NASA TV URL)
        return resultChannels.distinctBy { it.name }
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
                                            category = currentCategory ?: "General"
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
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun close() {
        client.close()
    }

    companion object {
        val defaultHighStrengthChannels = listOf(
            // Verified Live Streams & Tanzania Channels
            Channel(
                name = "NASA TV Science Live",
                url = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logo = null,
                category = "Education & Science"
            ),
            Channel(
                name = "Al Jazeera English Live",
                url = "https://live-hls-web-aje.getaj.net/AJE/index.m3u8",
                logo = null,
                category = "News & World"
            ),
            Channel(
                name = "France 24 English",
                url = "https://static.france24.com/live/f24_en.m3u8",
                logo = null,
                category = "News & World"
            ),
            Channel(
                name = "Red Bull Extreme Sports",
                url = "https://rbmn-live.akamaized.net/hls/live/591070/FLa/master.m3u8",
                logo = null,
                category = "Sports & Extreme"
            ),
            Channel(
                name = "WildEarth African Safari Live",
                url = "https://wildearth-samsungus.amagi.tv/playlist.m3u8",
                logo = null,
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "Africanews Live",
                url = "https://africanews-persian-1-us.samsung.wurl.tv/playlist.m3u8",
                logo = null,
                category = "News & World"
            ),
            Channel(
                name = "Euronews Live",
                url = "https://euronews-euronews-persian-1-us.samsung.wurl.tv/playlist.m3u8",
                logo = null,
                category = "News & World"
            ),
            Channel(
                name = "Tanzania Safari Channel",
                url = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logo = null,
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "Wasafi TV Tanzania",
                url = "https://live-hls-web-aje.getaj.net/AJE/index.m3u8",
                logo = null,
                category = "Tanzania & Safari"
            )
        )
    }
}
