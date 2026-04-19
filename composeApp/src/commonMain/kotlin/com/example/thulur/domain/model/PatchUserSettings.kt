package com.example.thulur.domain.model

/**
 * Partial app-facing user settings update.
 */
data class PatchUserSettings(
    val darkMode: Boolean? = null,
    val suggestionsOutside: Boolean? = null,
    val minQualityScore: Double? = null,
    val language: String? = null,
    val notificationsEnabled: Boolean? = null,
    val notificationsTime: String? = null,
    val timezone: String? = null,
)
