package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.DailyFeed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.usecase.GetDailyFeedUseCase
import com.example.thulur.presentation.composables.ThulurThreadArticleData
import com.example.thulur.presentation.composables.TopicsViewMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Shared Compose ViewModel driving the Daily Feed screen.
 */
class DailyFeedViewModel(
    private val getDailyFeedUseCase: GetDailyFeedUseCase,
) : ViewModel() {
    private val initialDay = currentDay()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        DailyFeedUiState(
            selectedDay = initialDay,
        ),
    )
    val uiState: StateFlow<DailyFeedUiState> = _uiState.asStateFlow()

    init {
        loadDay(initialDay)
    }

    fun onBackClick() {
        loadDay(_uiState.value.selectedDay.minus(1, DateTimeUnit.DAY))
    }

    fun onForwardClick() {
        val selectedDay = _uiState.value.selectedDay
        val today = currentDay()
        if (selectedDay >= today) return

        loadDay(selectedDay.plus(1, DateTimeUnit.DAY))
    }

    fun retry() {
        loadDay(_uiState.value.selectedDay)
    }

    fun onShowWholeSubjectClick(threadId: String, threadName: String) {
        _uiState.update { state ->
            state.copy(
                openThreadHistory = OpenThreadHistory(
                    threadId = threadId,
                    threadName = threadName,
                    initialDay = state.selectedDay,
                ),
            )
        }
    }

    fun onArticleClick(article: ThulurThreadArticleData) {
        val isRead = (_uiState.value.contentState as? DailyFeedContentState.Success)
            ?.threads?.flatMap { it.articles }?.find { it.id == article.id }?.isRead ?: false
        _uiState.update { state ->
            state.copy(
                openArticle = OpenArticle(
                    articleId = article.id,
                    title = article.title,
                    url = article.url,
                    isRead = isRead,
                ),
            )
        }
    }

    fun onCloseThreadHistory() {
        _uiState.update { state ->
            state.copy(openThreadHistory = null)
        }
    }

    fun onCloseArticleReader() {
        _uiState.update { state ->
            state.copy(openArticle = null)
        }
    }

    fun onFeedScrollStateChange(index: Int, offset: Int) {
        _uiState.update { state ->
            state.copy(feedScrollIndex = index, feedScrollOffset = offset)
        }
    }

    fun onTopicsViewModeChange(viewMode: TopicsViewMode) {
        _uiState.update { state ->
            val articleVisibilityByThreadId = when (val contentState = state.contentState) {
                is DailyFeedContentState.Success -> contentState.threads.associate { thread ->
                    thread.id to viewMode.defaultArticlesVisible()
                }

                DailyFeedContentState.Empty,
                DailyFeedContentState.Loading,
                is DailyFeedContentState.Error -> emptyMap()
            }

            state.copy(
                topicsViewMode = viewMode,
                articleVisibilityByThreadId = articleVisibilityByThreadId,
            )
        }
    }

    fun onThreadArticlesVisibilityToggle(threadId: String) {
        _uiState.update { state ->
            val currentValue = state.articleVisibilityByThreadId[threadId]
                ?: state.topicsViewMode.defaultArticlesVisible()
            state.copy(
                articleVisibilityByThreadId = state.articleVisibilityByThreadId + (threadId to !currentValue),
            )
        }
    }

    private fun loadDay(day: LocalDate) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    selectedDay = day,
                    isDefault = null,
                    articleVisibilityByThreadId = emptyMap(),
                    contentState = DailyFeedContentState.Loading,
                    feedScrollIndex = 0,
                    feedScrollOffset = 0,
                )
            }

            var loadedFeed: DailyFeed? = null
            val contentState = try {
                loadedFeed = getDailyFeedUseCase(day = day)
                if (loadedFeed.threads.isEmpty()) {
                    DailyFeedContentState.Empty
                } else {
                    DailyFeedContentState.Success(loadedFeed.threads)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                loadedFeed = null
                DailyFeedContentState.Error(
                    message = throwable.message ?: "Failed to load Daily Feed.",
                )
            }

            _uiState.update { state ->
                val articleVisibilityByThreadId = when (contentState) {
                    is DailyFeedContentState.Success -> contentState.threads.associate { thread ->
                        thread.id to state.topicsViewMode.defaultArticlesVisible()
                    }

                    DailyFeedContentState.Empty,
                    DailyFeedContentState.Loading,
                    is DailyFeedContentState.Error -> emptyMap()
                }

                state.copy(
                    selectedDay = day,
                    isDefault = loadedFeed?.isDefault,
                    articleVisibilityByThreadId = articleVisibilityByThreadId,
                    contentState = contentState,
                )
            }
        }
    }

    private fun currentDay(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
