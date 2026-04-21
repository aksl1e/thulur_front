package com.example.thulur.presentation.mainfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.RateArticleUseCase
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.theme.ThulurTheme
import org.koin.compose.koinInject

@Composable
fun ArticleReaderRoute(
    sessionInstanceId: Int,
    openArticle: OpenArticle,
    onBackClick: () -> Unit,
) {
    val getArticleParagraphsUseCase = koinInject<GetArticleParagraphsUseCase>()
    val rateArticleUseCase = koinInject<RateArticleUseCase>()
    val factory = remember(openArticle, getArticleParagraphsUseCase, rateArticleUseCase) {
        articleReaderViewModelFactory(
            openArticle = openArticle,
            getArticleParagraphsUseCase = getArticleParagraphsUseCase,
            rateArticleUseCase = rateArticleUseCase,
        )
    }
    val viewModel: ArticleReaderViewModel = viewModel(
        key = articleReaderViewModelKey(
            sessionInstanceId = sessionInstanceId,
            articleId = openArticle.articleId,
        ),
        factory = factory,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArticleReaderScreen(
        uiState = uiState,
        onBackClick = {
            viewModel.submitRate()
            onBackClick()
        },
        onInitialPageLoaded = viewModel::onInitialPageLoaded,
        onInjectionSucceeded = viewModel::onInjectionSucceeded,
        onProgressChanged = viewModel::onProgressChanged,
        onRateArticle = viewModel::onRateArticle,
        onError = viewModel::onBrowserError,
    )
}

internal fun articleReaderViewModelKey(
    sessionInstanceId: Int,
    articleId: String,
): String = "article-reader-session-$sessionInstanceId-article-$articleId"

@Composable
private fun ArticleReaderScreen(
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.slate.s50,
                shape = RoundedCornerShape(24.dp),
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

private fun articleReaderViewModelFactory(
    openArticle: OpenArticle,
    getArticleParagraphsUseCase: GetArticleParagraphsUseCase,
    rateArticleUseCase: RateArticleUseCase,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: kotlin.reflect.KClass<T>,
        extras: CreationExtras,
    ): T = ArticleReaderViewModel(
        openArticle = openArticle,
        getArticleParagraphsUseCase = getArticleParagraphsUseCase,
        rateArticleUseCase = rateArticleUseCase,
    ) as T
}
