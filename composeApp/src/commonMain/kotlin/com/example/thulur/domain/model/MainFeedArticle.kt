package com.example.thulur.domain.model

/**
 * App-facing article model used by the Main Feed feature.
 */
data class MainFeedArticle(
    val id: String,
    val feedId: String,
    val title: String,
    val url: String,
    val published: String?,
    val displaySummary: String?,
    val isRead: Boolean,
    val isSuggestion: Boolean,
    val quality: ArticleQuality,
) {
    /**
     * UI-oriented quality bucket derived from the raw backend score.
     */
    enum class ArticleQuality {
        Trash,
        Default,
        Important,
    }
}
