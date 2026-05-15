package com.example.thulur.presentation.dailyfeed.article_reader

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.thulur.domain.session.ReadArticlesCache
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.RateArticleUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticleReaderViewModel(
    articleId: String,
    title: String,
    url: String,
    isRead: Boolean,
    private val getArticleParagraphsUseCase: GetArticleParagraphsUseCase,
    private val rateArticleUseCase: RateArticleUseCase,
    private val readArticlesCache: ReadArticlesCache,
) : ScreenModel {
    private val _uiState = MutableStateFlow(
        ArticleReaderUiState(
            articleId = articleId,
            title = title,
            url = url,
            isArticleRead = isRead,
        ),
    )
    val uiState: StateFlow<ArticleReaderUiState> = _uiState.asStateFlow()

    init {
        loadParagraphs(articleId = articleId)
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

    suspend fun submitRate() {
        val state = _uiState.value
        if (state.isSubmittingRate) {
            println("[ThulurArticleReader] submitRate skipped articleId=${state.articleId} reason=already_submitting")
            return
        }

        if (state.rate <= 0 || state.isArticleRead) {
            println("[ThulurArticleReader] submitRate skipped articleId=${state.articleId} rate=${state.rate} isRead=${state.isArticleRead}")
            return
        }

        println("[ThulurArticleReader] submitRate articleId=${state.articleId} rate=${state.rate}")
        _uiState.update { currentState ->
            currentState.copy(isSubmittingRate = true)
        }

        try {
            rateArticleUseCase(articleId = state.articleId, rating = state.rate)
            readArticlesCache.markRead(state.articleId)
            _uiState.update { currentState ->
                currentState.copy(
                    isArticleRead = true,
                    isSubmittingRate = false,
                )
            }
            println("[ThulurArticleReader] submitRate SUCCESS articleId=${state.articleId} rate=${state.rate}")
        } catch (exception: CancellationException) {
            _uiState.update { currentState ->
                currentState.copy(isSubmittingRate = false)
            }
            throw exception
        } catch (throwable: Throwable) {
            _uiState.update { currentState ->
                currentState.copy(isSubmittingRate = false)
            }
            println("[ThulurArticleReader] submitRate FAILURE articleId=${state.articleId} rate=${state.rate} error=${throwable.message}")
        }
    }

    fun onBrowserError(message: String) {
        _uiState.update { state ->
            state.copy(errorMessage = message)
        }
    }

    private fun loadParagraphs(articleId: String) {
        screenModelScope.launch {
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
