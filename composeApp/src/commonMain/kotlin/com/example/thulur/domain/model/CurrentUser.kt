package com.example.thulur.domain.model

/**
 * App-facing current user model.
 */
data class CurrentUser(
    val id: String,
    val email: String?,
    val subscriptionTier: String,
    val subscriptionExpiresAt: String?,
    val createdAt: String,
)
