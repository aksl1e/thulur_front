package com.example.thulur_api.dtos.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponseDto(
    @SerialName("response")
    val response: String,
)
