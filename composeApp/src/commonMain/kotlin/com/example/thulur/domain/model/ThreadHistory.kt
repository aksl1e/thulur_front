package com.example.thulur.domain.model

/**
 * App-facing thread history model grouped by day.
 */
data class ThreadHistory(
    val threadId: String,
    val threadName: String,
    val days: List<ThreadHistoryDay>,
)
