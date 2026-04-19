package com.example.thulur.domain.model

import kotlinx.datetime.LocalDate

/**
 * App-facing thread model used by the Main Feed feature.
 */
data class MainFeedThread(
    val id: String,
    val name: String,
    val topicId: String?,
    val topicName: String?,
    val mainFeedScore: Double,
    val firstSeen: LocalDate?,
    val summary: String?,
    val articles: List<MainFeedArticle>,
)
