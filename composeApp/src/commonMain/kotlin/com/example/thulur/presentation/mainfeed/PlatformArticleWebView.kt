package com.example.thulur.presentation.mainfeed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.thulur.domain.model.ArticleParagraph

@Composable
internal expect fun PlatformArticleWebView(
    initialUrl: String,
    paragraphs: List<ArticleParagraph>,
    areParagraphsReady: Boolean,
    modifier: Modifier = Modifier,
    onInitialPageLoaded: () -> Unit,
    onInjectionSucceeded: () -> Unit,
    onProgressChanged: (Float) -> Unit,
    onError: (String) -> Unit,
)
