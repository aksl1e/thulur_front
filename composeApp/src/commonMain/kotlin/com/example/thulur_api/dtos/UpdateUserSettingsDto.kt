package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Partial user settings update payload sent to the backend.
 *
 * Null properties are intentionally omitted from JSON by the shared client.
 */
@Serializable
data class UpdateUserSettingsDto(
    @SerialName("dark_mode")
    val darkMode: Boolean? = null,
    @SerialName("suggestions_outside")
    val suggestionsOutside: Boolean? = null,
    @SerialName("min_quality_score")
    val minQualityScore: Double? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean? = null,
    @SerialName("notifications_time")
    val notificationsTime: String? = null,
    @SerialName("timezone")
    val timezone: String? = null,
)
