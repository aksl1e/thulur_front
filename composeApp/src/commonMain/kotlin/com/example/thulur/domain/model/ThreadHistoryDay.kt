package com.example.thulur.domain.model

import kotlinx.datetime.LocalDate

/**
 * App-facing day model inside thread history.
 */
data class ThreadHistoryDay(
    val day: LocalDate,
    val threadSummary: String?,
    val articles: List<Article>,
)
