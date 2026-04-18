package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.ArticleParagraph

data class ArticleReaderUiState(
    val articleId: String,
    val title: String,
    val url: String,
    val paragraphs: List<ArticleParagraph> = emptyList(),
    val areParagraphsLoaded: Boolean = false,
    val isInitialPageLoaded: Boolean = false,
    val isInjectionApplied: Boolean = false,
    val readProgress: Float = 0f,
    val errorMessage: String? = null,
) {
    val isReady: Boolean
        get() = areParagraphsLoaded && isInitialPageLoaded && isInjectionApplied && errorMessage == null
}
