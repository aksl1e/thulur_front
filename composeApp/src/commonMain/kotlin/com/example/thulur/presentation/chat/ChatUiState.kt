package com.example.thulur.presentation.chat

import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.presentation.composables.TopicsViewMode
import com.example.thulur.presentation.dailyfeed.OpenThreadHistory
import kotlinx.datetime.LocalDate
data class ChatUiState(
    val contentState: ChatContentState = ChatContentState.Loading,
    val selectedThreadId: String? = null,
)
sealed interface ChatContentState {
    data object Loading : ChatContentState
    data object Empty : ChatContentState
    data class Error(val message: String) : ChatContentState
    data class Success(val threads: List<DailyFeedThread>) : ChatContentState
}