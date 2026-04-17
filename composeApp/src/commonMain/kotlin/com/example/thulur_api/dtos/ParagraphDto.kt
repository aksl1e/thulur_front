package com.example.thulur_api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw paragraph payload returned by the backend article paragraphs endpoint.
 */
@Serializable
data class ParagraphDto(
    @SerialName("idx")
    val idx: Int,
    @SerialName("text")
    val text: String,
    @SerialName("is_novel")
    val isNovel: Boolean,
)
