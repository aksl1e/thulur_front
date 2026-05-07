package com.example.thulur.presentation.dailyfeed.article_reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.session.ReadArticlesCache
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.RateArticleUseCase
import com.example.thulur.presentation.dailyfeed.OpenArticle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticleReaderViewModel(
    openArticle: OpenArticle,
    private val getArticleParagraphsUseCase: GetArticleParagraphsUseCase,
    private val rateArticleUseCase: RateArticleUseCase,
    private val readArticlesCache: ReadArticlesCache,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ArticleReaderUiState(
            articleId = openArticle.articleId,
            title = openArticle.title,
            url = openArticle.url,
            isArticleRead = openArticle.isRead,
        ),
    )
    val uiState: StateFlow<ArticleReaderUiState> = _uiState.asStateFlow()

    init {
        loadParagraphs(articleId = openArticle.articleId)
    }

    fun onInitialPageLoaded() {
        _uiState.update { state ->
            if (state.isInitialPageLoaded) state else state.copy(isInitialPageLoaded = true)
        }
    }

    fun onInjectionSucceeded() {
        _uiState.update { state ->
            if (state.isInjectionApplied) state else state.copy(isInjectionApplied = true)
        }
    }

    fun onProgressChanged(progress: Float) {
        val clampedProgress = progress.coerceIn(0f, 1f)
        _uiState.update { state ->
            state.copy(readProgress = maxOf(state.readProgress, clampedProgress))
        }
    }

    fun onRateArticle(rate: Int) {
        _uiState.update { state ->
            state.copy(rate = maxOf(state.rate, rate))
        }
    }

    fun submitRate() {
        val state = _uiState.value
        if (state.rate > 0 && !state.isArticleRead) {
            println("[ThulurArticleReader] submitRate articleId=${state.articleId} rate=${state.rate}")
            viewModelScope.launch {
                runCatching { rateArticleUseCase(articleId = state.articleId, rating = state.rate) }
                    .onSuccess {
                        readArticlesCache.markRead(state.articleId)
                        _uiState.update { currentState ->
                            currentState.copy(isArticleRead = true)
                        }
                        println("[ThulurArticleReader] submitRate SUCCESS articleId=${state.articleId} rate=${state.rate}")
                    }
                    .onFailure { println("[ThulurArticleReader] submitRate FAILURE articleId=${state.articleId} rate=${state.rate} error=${it.message}") }
            }
        } else {
            println("[ThulurArticleReader] submitRate skipped articleId=${state.articleId} rate=${state.rate} isRead=${state.isArticleRead}")
        }
    }

    fun onBrowserError(message: String) {
        _uiState.update { state ->
            state.copy(errorMessage = message)
        }
    }

    private fun loadParagraphs(articleId: String) {
        viewModelScope.launch {
            val result = try {
                Result.success(getArticleParagraphsUseCase(articleId))
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = { paragraphs ->
                        state.copy(
                            paragraphs = paragraphs,
                            areParagraphsLoaded = true,
                        )
                    },
                    onFailure = { throwable ->
                        state.copy(
                            errorMessage = throwable.message ?: "Failed to load article paragraphs.",
                        )
                    },
                )
            }
        }
    }
}
