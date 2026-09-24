package org.com

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentEpochMillis(): Long

expect fun formatEpochMillis(millis: Long): String