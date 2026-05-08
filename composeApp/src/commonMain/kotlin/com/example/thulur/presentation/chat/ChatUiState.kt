package com.example.thulur.presentation.chat

import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.presentation.composables.TopicsViewMode
import com.example.thulur.presentation.dailyfeed.OpenThreadHistory
import kotlinx.datetime.LocalDate
data class ChatUiState(
    val contentState: ChatContentState = ChatContentState.Loading,
    val selectedThreadId: String? = null,
    val inputValue: String = "",
    val messages: List<Message> = listOf(
        Message(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", isUser = true),
        Message(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", isUser = false),
    ),
)
data class Message(
    val text: String,
    val isUser: Boolean,
)

sealed interface ChatContentState {
    data object Loading : ChatContentState
    data object Empty : ChatContentState
    data class Error(val message: String) : ChatContentState
    data class Success(val threads: List<DailyFeedThread>) : ChatContentState
}