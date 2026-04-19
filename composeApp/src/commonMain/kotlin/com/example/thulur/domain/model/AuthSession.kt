package com.example.thulur.domain.model

/**
 * App-facing authenticated session model.
 */
data class AuthSession(
    val sessionId: String,
    val deviceName: String,
    val platform: String,
    val city: String?,
    val country: String?,
    val lastSeenAt: String,
)
