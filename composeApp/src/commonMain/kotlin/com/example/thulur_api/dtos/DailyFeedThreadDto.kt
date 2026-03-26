package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw daily feed thread payload returned by the backend.
 */
@Serializable
data class DailyFeedThreadDto(
    /** Thread's id. */
    @SerialName("thread_id")
    val threadId: String,
    /** Thread's name. */
    @SerialName("thread_name")
    val threadName: String,
    /** Topic's id. */
    @SerialName("topic_id")
    val topicId: String?,
    /** Topic's name. */
    @SerialName("topic_name")
    val topicName: String?,
    /** Thread's main feed score used for sorting inside the feed. */
    @SerialName("main_feed_score")
    val mainFeedScore: Double,
    /**
     * Date from the last 7 days telling whether the thread was already shown.
     *
     * If the backend sends `9999-12-31`, the thread has not been shown before.
     */
    @SerialName("thread_first_seen")
    val threadFirstSeen: String?,
    /** Thread's summary. */
    @SerialName("thread_summary")
    val threadSummary: String?,
    /** Articles within the thread. */
    @SerialName("articles")
    val articles: List<DailyFeedArticleDto> = emptyList(),
)
