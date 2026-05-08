package com.example.thulur_api.dtos.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    @SerialName("message")
    val message: String,
)
