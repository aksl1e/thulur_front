package com.example.thulur.domain.model

/**
 * App-facing daily feed model used by the Daily Feed feature.
 */
data class DailyFeed(
    val isDefault: Boolean,
    val threads: List<DailyFeedThread>,
)
