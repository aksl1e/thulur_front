package com.example.thulur.presentation.dailyfeed.article_reader

import com.example.thulur.domain.model.ArticleParagraph

data class ArticleReaderUiState(
    val articleId: String,
    val title: String,
    val url: String,
    val isArticleRead: Boolean = false,
    val isSubmittingRate: Boolean = false,
    val paragraphs: List<ArticleParagraph> = emptyList(),
    val areParagraphsLoaded: Boolean = false,
    val isInitialPageLoaded: Boolean = false,
    val isInjectionApplied: Boolean = false,
    val readProgress: Float = 0f,
    val rate: Int = 0,
    val errorMessage: String? = null,
) {
    val isReady: Boolean
        get() = areParagraphsLoaded && isInitialPageLoaded && isInjectionApplied && errorMessage == null
}
