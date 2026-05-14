package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.DailyFeed
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.thulur.domain.usecase.GetDailyFeedUseCase
import com.example.thulur.domain.session.ReadArticlesCache
import com.example.thulur.presentation.composables.TopicsViewMode
import com.example.thulur.presentation.dailyfeed.article_reader.applyCachedReadArticlesToThreads
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
    private val readArticlesCache: ReadArticlesCache,
) : ScreenModel {
    private val initialDay = currentDay()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        DailyFeedUiState(
            selectedDay = initialDay,
        ),
    )
    val uiState: StateFlow<DailyFeedUiState> = _uiState.asStateFlow()

    init {
        observeReadArticles()
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

    private fun observeReadArticles() {
        screenModelScope.launch {
            readArticlesCache.readArticles.collect { readArticles ->
                _uiState.update { state -> state.applyCachedReadArticles(readArticles) }
            }
        }
    }

    private fun loadDay(day: LocalDate) {
        loadJob?.cancel()
        loadJob = screenModelScope.launch {
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

private fun DailyFeedUiState.applyCachedReadArticles(
    readArticles: Map<String, Boolean>,
): DailyFeedUiState {
    val successState = contentState as? DailyFeedContentState.Success
    if (successState == null) {
        return this
    }

    val updatedThreads = successState.threads.applyCachedReadArticlesToThreads(readArticles)
    if (updatedThreads === successState.threads) {
        return this
    }

    return copy(
        contentState = if (updatedThreads === successState.threads) {
            contentState
        } else {
            DailyFeedContentState.Success(updatedThreads)
        },
    )
}
