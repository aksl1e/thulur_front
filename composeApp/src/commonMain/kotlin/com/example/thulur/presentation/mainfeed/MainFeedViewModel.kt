package com.example.thulur.presentation.mainfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.usecase.GetMainFeedUseCase
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
import kotlin.time.ExperimentalTime

/**
 * Shared Compose ViewModel driving the Main Feed screen.
 */
@OptIn(ExperimentalTime::class)
class MainFeedViewModel(
    private val getMainFeedUseCase: GetMainFeedUseCase,
) : ViewModel() {
    private val initialDay = currentDay()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        MainFeedUiState(
            selectedDay = initialDay,
        ),
    )
    val uiState: StateFlow<MainFeedUiState> = _uiState.asStateFlow()

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

    fun onTopicsViewModeChange(viewMode: TopicsViewMode) {
        _uiState.update { state ->
            val articleVisibilityByThreadId = when (val contentState = state.contentState) {
                is MainFeedContentState.Success -> contentState.threads.associate { thread ->
                    thread.id to viewMode.defaultArticlesVisible()
                }

                MainFeedContentState.Empty,
                MainFeedContentState.Loading,
                is MainFeedContentState.Error -> emptyMap()
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
                    articleVisibilityByThreadId = emptyMap(),
                    contentState = MainFeedContentState.Loading,
                )
            }

            val contentState = try {
                val threads = getMainFeedUseCase(day = day)
                if (threads.isEmpty()) {
                    MainFeedContentState.Empty
                } else {
                    MainFeedContentState.Success(threads)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                MainFeedContentState.Error(
                    message = throwable.message ?: "Failed to load Main Feed.",
                )
            }

            _uiState.update { state ->
                val articleVisibilityByThreadId = when (contentState) {
                    is MainFeedContentState.Success -> contentState.threads.associate { thread ->
                        thread.id to state.topicsViewMode.defaultArticlesVisible()
                    }

                    MainFeedContentState.Empty,
                    MainFeedContentState.Loading,
                    is MainFeedContentState.Error -> emptyMap()
                }

                state.copy(
                    selectedDay = day,
                    articleVisibilityByThreadId = articleVisibilityByThreadId,
                    contentState = contentState,
                )
            }
        }
    }

    private fun currentDay(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
