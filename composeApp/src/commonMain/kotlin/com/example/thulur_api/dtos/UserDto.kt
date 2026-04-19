package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw current-user payload returned by `/users/me`.
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("subscription_tier")
    val subscriptionTier: String,
    @SerialName("subscription_expires_at")
    val subscriptionExpiresAt: String? = null,
    @SerialName("created_at")
    val createdAt: String,
)
