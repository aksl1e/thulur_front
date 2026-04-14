package com.example.thulur_api.dtos.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Successful desktop auth exchange payload used by the app.
 */
@Serializable
data class AuthTokenDto(
    @SerialName("token")
    val token: String,
)
