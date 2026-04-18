package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw thread history payload returned by the backend.
 */
@Serializable
data class ThreadHistoryDto(
    /** Thread's id. */
    @SerialName("thread_id")
    val threadId: String,
    /** Thread's name. */
    @SerialName("thread_name")
    val threadName: String,
    /** History grouped by day. */
    @SerialName("days")
    val days: List<ThreadHistoryDayDto> = emptyList(),
)
