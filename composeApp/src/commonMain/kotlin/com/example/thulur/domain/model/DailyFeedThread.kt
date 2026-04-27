package com.example.thulur.domain.model

import kotlinx.datetime.LocalDate

/**
 * App-facing thread model used by the Daily Feed feature.
 */
data class DailyFeedThread(
    val id: String,
    val name: String,
    val topicId: String?,
    val topicName: String?,
    val dailyFeedScore: Double,
    val firstSeen: LocalDate?,
    val summary: String?,
    val articles: List<Article>,
)
