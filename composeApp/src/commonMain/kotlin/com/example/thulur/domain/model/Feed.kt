package com.example.thulur.domain.model

/**
 * App-facing feed model used by settings and feed management features.
 */
data class Feed(
    val id: String,
    val url: String,
    val language: String?,
    val tags: List<String>,
    val createdAt: String,
)
