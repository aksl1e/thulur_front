package com.example.thulur.presentation.mainfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticleReaderViewModel(
    openArticle: OpenArticle,
    private val getArticleParagraphsUseCase: GetArticleParagraphsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ArticleReaderUiState(
            articleId = openArticle.articleId,
            title = openArticle.title,
            url = openArticle.url,
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
