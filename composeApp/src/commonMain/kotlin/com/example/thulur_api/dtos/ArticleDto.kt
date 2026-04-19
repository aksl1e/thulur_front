package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw article payload reused across backend responses.
 */
@Serializable
data class ArticleDto(
    /** Article's id. */
    @SerialName("article_id")
    val articleId: String,
    /** Feed's id. */
    @SerialName("feed_id")
    val feedId: String,
    /** Article's title. */
    @SerialName("title")
    val title: String,
    /** Article's url. */
    @SerialName("url")
    val url: String,
    /** Article's publication date from RSS. */
    @SerialName("published")
    val published: String?,
    /** UI-oriented article quality tier from the backend. */
    @SerialName("quality_tier")
    val qualityTier: String?,
    /** Summary version intended for UI display. */
    @SerialName("display_summary")
    val displaySummary: String?,
    /** Flag showing whether the article was read. */
    @SerialName("is_read")
    val isRead: Boolean,
    /** Flag showing whether the article was suggested. */
    @SerialName("is_suggestion")
    val isSuggestion: Boolean,
)
