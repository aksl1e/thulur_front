package com.example.thulur.presentation.dailyfeed.thread_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.ThreadHistoryDay
import com.example.thulur.domain.session.ReadArticlesCache
import com.example.thulur.domain.usecase.GetThreadHistoryUseCase
import com.example.thulur.presentation.dailyfeed.article_reader.applyCachedReadArticles
import com.example.thulur.presentation.dailyfeed.OpenThreadHistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class ThreadHistoryViewModel(
    private val openThreadHistory: OpenThreadHistory,
    private val getThreadHistoryUseCase: GetThreadHistoryUseCase,
    private val readArticlesCache: ReadArticlesCache,
) : ViewModel() {
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        ThreadHistoryUiState(
            threadName = openThreadHistory.threadName,
        ),
    )
    val uiState: StateFlow<ThreadHistoryUiState> = _uiState.asStateFlow()

    init {
        observeReadArticles()
        load()
    }

    fun retry() {
        load()
    }

    fun onPreviousDayClick(): Boolean = changeVisibleDayBy(delta = 1)

    fun onNextDayClick(): Boolean = changeVisibleDayBy(delta = -1)

    private fun observeReadArticles() {
        viewModelScope.launch {
            readArticlesCache.readArticles.collect { readArticles ->
                _uiState.update { state ->
                    val contentState = state.contentState as? ThreadHistoryContentState.Success ?: return@update state
                    val updatedHistory = contentState.history.applyCachedReadArticles(readArticles)
                    if (updatedHistory === contentState.history) {
                        state
                    } else {
                        state.copy(
                            contentState = contentState.copy(history = updatedHistory),
                        )
                    }
                }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(contentState = ThreadHistoryContentState.Loading)
            }

            val nextState = try {
                val history = getThreadHistoryUseCase(threadId = openThreadHistory.threadId)
                    .sortedByDescendingDay()

                if (history.days.isEmpty()) {
                    ThreadHistoryUiState(
                        threadName = history.threadName.ifBlank { openThreadHistory.threadName },
                        contentState = ThreadHistoryContentState.Empty,
                    )
                } else {
                    ThreadHistoryUiState(
                        threadName = history.threadName.ifBlank { openThreadHistory.threadName },
                        contentState = ThreadHistoryContentState.Success(
                            history = history,
                            visibleDayIndex = resolveInitialDayIndex(
                                days = history.days,
                                initialDay = openThreadHistory.initialDay,
                            ),
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                ThreadHistoryUiState(
                    threadName = openThreadHistory.threadName,
                    contentState = ThreadHistoryContentState.Error(
                        message = throwable.message ?: "Failed to load thread history.",
                    ),
                )
            }

            _uiState.value = nextState
        }
    }

    private fun changeVisibleDayBy(delta: Int): Boolean {
        var didChange = false

        _uiState.update { state ->
            val contentState = state.contentState as? ThreadHistoryContentState.Success ?: return@update state
            val nextIndex = (contentState.visibleDayIndex + delta)
                .coerceIn(0, contentState.history.days.lastIndex)

            if (nextIndex == contentState.visibleDayIndex) {
                return@update state
            }

            didChange = true
            state.copy(
                contentState = contentState.copy(
                    visibleDayIndex = nextIndex,
                ),
            )
        }

        return didChange
    }
}

internal fun ThreadHistory.sortedByDescendingDay(): ThreadHistory = copy(
    days = days.sortedByDescending(ThreadHistoryDay::day),
)

internal fun resolveInitialDayIndex(
    days: List<ThreadHistoryDay>,
    initialDay: LocalDate,
): Int {
    val exactMatchIndex = days.indexOfFirst { day -> day.day == initialDay }
    if (exactMatchIndex >= 0) return exactMatchIndex

    val nearestOlderIndex = days.indexOfFirst { day -> day.day <= initialDay }
    if (nearestOlderIndex >= 0) return nearestOlderIndex

    return 0
}
