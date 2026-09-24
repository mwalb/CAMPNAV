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

        // Deduplicate by URL or Name
        return resultChannels.distinctBy { it.url }
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
            // Tanzania & Safari Channels
            Channel(
                name = "TBC Safari Tanzania",
                url = "https://cdn.jmk.sh/hls/tbcsafari/live.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Tanzania_coat_of_arms.svg/200px-Tanzania_coat_of_arms.svg.png",
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "WildEarth African Safari Live",
                url = "https://wildearth-samsungus.amagi.tv/playlist.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e4/WildEarth_Logo.png/220px-WildEarth_Logo.png",
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "Wasafi TV Tanzania",
                url = "https://wasafitv.cdn.ngenix.net/live/wasafitv/playlist.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Wasafi_FM_Logo.png/220px-Wasafi_FM_Logo.png",
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "TBC 1 Tanzania Live",
                url = "https://cdn.jmk.sh/hls/tbc1/live.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Tanzania_coat_of_arms.svg/200px-Tanzania_coat_of_arms.svg.png",
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "ITV Tanzania",
                url = "https://live.itv.co.tz/hls/stream.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/en/2/29/ITV_Tanzania_logo.png",
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "Clouds TV Tanzania",
                url = "https://cloudstv.cdn.ngenix.net/live/cloudstv/playlist.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/en/0/07/Clouds_TV_Logo.png",
                category = "Tanzania & Safari"
            ),
            Channel(
                name = "East Africa TV (EATV)",
                url = "https://eatv.cdn.ngenix.net/live/eatv/playlist.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/en/5/52/EATV_Logo.png",
                category = "Tanzania & Safari"
            ),

            // Global News & World
            Channel(
                name = "Africanews Live",
                url = "https://africanews-persian-1-us.samsung.wurl.tv/playlist.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Africanews_logo.svg/1200px-Africanews_logo.svg.png",
                category = "News & World"
            ),
            Channel(
                name = "Al Jazeera English Live",
                url = "https://live-hls-web-aje.getaj.net/AJE/index.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/en/thumb/7/75/Al_Jazeera_English_logo.svg/1200px-Al_Jazeera_English_logo.svg.png",
                category = "News & World"
            ),
            Channel(
                name = "DW News Live",
                url = "https://dwamdstream102-lh.akamaihd.net/i/dwstream102_1@115641/master.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Deutsche_Welle_2012.svg/1200px-Deutsche_Welle_2012.svg.png",
                category = "News & World"
            ),
            Channel(
                name = "France 24 English",
                url = "https://static.france24.com/live/f24_en.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/France_24_logo.svg/1200px-France_24_logo.svg.png",
                category = "News & World"
            ),

            // Education, Sports & Culture
            Channel(
                name = "NASA TV Science Live",
                url = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/1200px-NASA_logo.svg.png",
                category = "Education & Science"
            ),
            Channel(
                name = "Red Bull Extreme Sports",
                url = "https://rbmn-live.akamaized.net/hls/live/591070/FLa/master.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e2/Red_Bull_TV_logo.svg/1200px-Red_Bull_TV_logo.svg.png",
                category = "Sports & Extreme"
            ),
            Channel(
                name = "Euronews Live",
                url = "https://euronews-euronews-persian-1-us.samsung.wurl.tv/playlist.m3u8",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Euronews_2016_logo.svg/1200px-Euronews_2016_logo.svg.png",
                category = "News & World"
            )
        )
    }
}
