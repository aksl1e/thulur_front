package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw user settings payload returned by the backend.
 */
@Serializable
data class UserSettingsDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("dark_mode")
    val darkMode: Boolean,
    @SerialName("suggestions_outside")
    val suggestionsOutside: Boolean,
    @SerialName("min_quality_score")
    val minQualityScore: Double,
    @SerialName("language")
    val language: String,
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean,
    @SerialName("notifications_time")
    val notificationsTime: String,
    @SerialName("timezone")
    val timezone: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
