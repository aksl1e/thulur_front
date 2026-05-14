package com.example.thulur.presentation.chat

import com.example.thulur.presentation.router.ChatMode

data class ChatUiState(
    val title: String = "",
    val mode: ChatMode = ChatMode.General,
    val inputValue: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
)

sealed interface ChatMessage {
    data class User(val text: String) : ChatMessage

    data class Assistant(
        val markdown: String,
        val isError: Boolean = false,
    ) : ChatMessage

    data object AssistantPending : ChatMessage
}
