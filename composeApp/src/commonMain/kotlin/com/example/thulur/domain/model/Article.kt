package com.example.thulur.domain.model

/**
 * App-facing article model reused by feed and history features.
 */
data class Article(
    val id: String,
    val feedId: String,
    val title: String,
    val url: String,
    val published: String?,
    val displaySummary: String?,
    val isRead: Boolean,
    val isSuggestion: Boolean,
    val quality: ArticleQuality,
)
