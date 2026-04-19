package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw day payload inside thread history.
 */
@Serializable
data class ThreadHistoryDayDto(
    /** History day in ISO format. */
    @SerialName("day")
    val day: String,
    /** Thread summary for the specific day. */
    @SerialName("thread_summary")
    val threadSummary: String?,
    /** Articles within the day. */
    @SerialName("articles")
    val articles: List<ArticleDto> = emptyList(),
)
