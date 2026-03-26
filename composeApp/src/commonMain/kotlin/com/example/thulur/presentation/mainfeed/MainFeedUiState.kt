package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.presentation.composables.TopicsViewMode
import kotlinx.datetime.LocalDate

/**
 * UI state for the Main Feed screen and its app bar controls.
 */
data class MainFeedUiState(
    val selectedDay: LocalDate,
    val topicsViewMode: TopicsViewMode = TopicsViewMode.TopicsAndArticles,
    val contentState: MainFeedContentState = MainFeedContentState.Loading,
)

sealed interface MainFeedContentState {
    data object Loading : MainFeedContentState

    data object Empty : MainFeedContentState

    data class Success(
        val threads: List<MainFeedThread>,
    ) : MainFeedContentState

    data class Error(
        val message: String,
    ) : MainFeedContentState
}
