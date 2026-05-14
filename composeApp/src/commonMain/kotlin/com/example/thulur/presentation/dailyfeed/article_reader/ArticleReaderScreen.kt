package com.example.thulur.presentation.dailyfeed.article_reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDefaultShape

@Composable
fun ArticleReaderScreen(
    uiState: ArticleReaderUiState,
    onBackClick: () -> Unit,
    onInitialPageLoaded: () -> Unit,
    onInjectionSucceeded: () -> Unit,
    onProgressChanged: (Float) -> Unit,
    onRateArticle: (Int) -> Unit,
    onError: (String) -> Unit,
) {
    val loadingColors = ThulurTheme.SemanticColors.rootLoadingScreen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(loadingColors.screenBackground)
            .safeContentPadding(),
    ) {
        ThulurAppBar(
            title = uiState.title,
            backLabel = "Back",
            onBackClick = onBackClick,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            PlatformArticleWebView(
                initialUrl = uiState.url,
                paragraphs = uiState.paragraphs,
                areParagraphsReady = uiState.areParagraphsLoaded,
                isArticleRead = uiState.isArticleRead,
                modifier = Modifier.fillMaxSize(),
                onInitialPageLoaded = onInitialPageLoaded,
                onInjectionSucceeded = onInjectionSucceeded,
                onProgressChanged = onProgressChanged,
                onRateArticle = onRateArticle,
                onError = onError,
            )
            if (uiState.errorMessage == null && !uiState.isReady) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = loadingColors.indicatorColor,
                )
            }

            if (uiState.errorMessage != null) {
                ArticleReaderErrorState(
                    message = uiState.errorMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun ArticleReaderErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.Colors
    val shape = thulurDefaultShape()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.slate.s50,
                shape = shape,
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BasicText(
            text = "Article Reader Failed",
            style = ThulurTheme.Typography.headlineMedium.copy(color = colors.slate.s900),
        )
        BasicText(
            text = message,
            style = ThulurTheme.Typography.bodyLarge.copy(color = colors.slate.s700),
        )
    }
}
