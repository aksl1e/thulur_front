package com.example.thulur.presentation.mainfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.usecase.GetMainFeedUseCase
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
 * Shared Compose ViewModel driving the Main Feed screen.
 */
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
        val isRead = (_uiState.value.contentState as? MainFeedContentState.Success)
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
                    feedScrollIndex = 0,
                    feedScrollOffset = 0,
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
