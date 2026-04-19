package com.example.thulur.presentation.mainfeed.thread_history

import com.example.thulur.domain.model.ThreadHistory

data class ThreadHistoryUiState(
    val threadName: String,
    val contentState: ThreadHistoryContentState = ThreadHistoryContentState.Loading,
)

sealed interface ThreadHistoryContentState {
    data object Loading : ThreadHistoryContentState

    data object Empty : ThreadHistoryContentState

    data class Error(
        val message: String,
    ) : ThreadHistoryContentState

    data class Success(
        val history: ThreadHistory,
        val visibleDayIndex: Int,
    ) : ThreadHistoryContentState {
        val canGoToPreviousDay: Boolean
            get() = visibleDayIndex < history.days.lastIndex

        val canGoToNextDay: Boolean
            get() = visibleDayIndex > 0
    }
}
