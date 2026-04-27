package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw daily feed payload returned by the backend.
 */
@Serializable
data class DailyFeedDto(
    @SerialName("is_default")
    val isDefault: Boolean,
    @SerialName("threads")
    val threads: List<DailyFeedThreadDto> = emptyList(),
)
