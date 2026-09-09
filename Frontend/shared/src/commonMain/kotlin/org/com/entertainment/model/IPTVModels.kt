package org.com.entertainment.model

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val category: String? = null
)

data class PlayerState(
    val isBuffering: Boolean = false,
    val error: String? = null
)
