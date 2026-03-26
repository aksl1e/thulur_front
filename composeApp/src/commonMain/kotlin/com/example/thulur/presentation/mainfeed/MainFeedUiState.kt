package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.MainFeedThread

/**
 * UI state for the technical Main Feed screen.
 */
sealed interface MainFeedUiState {
    data object Loading : MainFeedUiState

    data object Empty : MainFeedUiState

    data class Success(
        val threads: List<MainFeedThread>,
    ) : MainFeedUiState

    data class Error(
        val message: String,
    ) : MainFeedUiState
}
