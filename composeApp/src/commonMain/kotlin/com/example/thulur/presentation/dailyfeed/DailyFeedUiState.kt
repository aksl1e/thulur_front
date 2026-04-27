package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.presentation.composables.TopicsViewMode
import kotlinx.datetime.LocalDate

/**
 * UI state for the Daily Feed screen and its app bar controls.
 */
data class DailyFeedUiState(
    val selectedDay: LocalDate,
    val isDefault: Boolean? = null,
    val topicsViewMode: TopicsViewMode = TopicsViewMode.TopicsAndArticles,
    val articleVisibilityByThreadId: Map<String, Boolean> = emptyMap(),
    val contentState: DailyFeedContentState = DailyFeedContentState.Loading,
    val openThreadHistory: OpenThreadHistory? = null,
    val openArticle: OpenArticle? = null,
    val feedScrollIndex: Int = 0,
    val feedScrollOffset: Int = 0,
)

data class OpenThreadHistory(
    val threadId: String,
    val threadName: String,
    val initialDay: LocalDate,
)

data class OpenArticle(
    val articleId: String,
    val title: String,
    val url: String,
    val isRead: Boolean = false,
)

sealed interface DailyFeedContentState {
    data object Loading : DailyFeedContentState

    data object Empty : DailyFeedContentState

    data class Success(
        val threads: List<DailyFeedThread>,
    ) : DailyFeedContentState

    data class Error(
        val message: String,
    ) : DailyFeedContentState
}

internal fun TopicsViewMode.defaultArticlesVisible(): Boolean = when (this) {
    TopicsViewMode.TopicsAndArticles -> true
    TopicsViewMode.TopicsOnly -> false
}
