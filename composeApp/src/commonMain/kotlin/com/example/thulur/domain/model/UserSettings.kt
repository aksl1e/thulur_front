package com.example.thulur.domain.model

/**
 * App-facing user settings model.
 */
data class UserSettings(
    val userId: String,
    val darkMode: Boolean,
    val suggestionsOutside: Boolean,
    val minQualityScore: Double,
    val language: String,
    val notificationsEnabled: Boolean,
    val notificationsTime: String,
    val timezone: String,
    val updatedAt: String,
)
