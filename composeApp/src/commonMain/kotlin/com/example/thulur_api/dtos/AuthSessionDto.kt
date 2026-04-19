package com.example.thulur_api.dtos

import kotlinx.serialization.Serializable

/**
 * Raw auth session payload returned by `/auth/sessions`.
 */
@Serializable
data class AuthSessionDto(
    val sessionId: String,
    val deviceName: String,
    val platform: String,
    val city: String? = null,
    val country: String? = null,
    val lastSeenAt: String,
)
