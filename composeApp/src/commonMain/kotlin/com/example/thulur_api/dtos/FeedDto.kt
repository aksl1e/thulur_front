package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw feed payload returned by backend feed endpoints.
 */
@Serializable
data class FeedDto(
    @SerialName("id")
    val id: String,
    @SerialName("url")
    val url: String,
    @SerialName("language")
    val language: String? = null,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
)
