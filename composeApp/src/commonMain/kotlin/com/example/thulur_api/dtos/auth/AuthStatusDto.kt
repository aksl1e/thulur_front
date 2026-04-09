package com.example.thulur_api.dtos.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal auth completion response returned by the backend.
 */
@Serializable
data class AuthStatusDto(
    @SerialName("status")
    val status: String,
)
