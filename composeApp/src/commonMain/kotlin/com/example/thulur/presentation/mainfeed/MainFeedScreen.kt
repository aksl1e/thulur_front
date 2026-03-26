package com.example.thulur.presentation.mainfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.domain.model.MainFeedArticle
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainFeedRoute(
    viewModel: MainFeedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainFeedScreen(
        uiState = uiState,
        onRetry = viewModel::refresh,
    )
}

@Composable
fun MainFeedScreen(
    uiState: MainFeedUiState,
    onRetry: () -> Unit,
) {
    val colors = mainFeedColors()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeContentPadding()
            .padding(24.dp),
    ) {
        when (uiState) {
            MainFeedUiState.Loading -> MainFeedStatusCard(
                title = "Loading Main Feed",
                body = "Requesting daily_feed from Thulur API.",
                colors = colors,
            )

            MainFeedUiState.Empty -> MainFeedStatusCard(
                title = "Main Feed Is Empty",
                body = "The backend returned an empty list. This is expected while the database is still empty.",
                colors = colors,
            )

            is MainFeedUiState.Error -> MainFeedErrorCard(
                message = uiState.message,
                colors = colors,
                onRetry = onRetry,
            )

            is MainFeedUiState.Success -> MainFeedSuccessContent(
                threads = uiState.threads,
                colors = colors,
            )
        }
    }
}

@Composable
private fun MainFeedStatusCard(
    title: String,
    body: String,
    colors: MainFeedColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outline, RoundedCornerShape(28.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText(
            text = title,
            style = ThulurTheme.Typography.headlineMedium.copy(color = colors.onSurface),
        )
        BasicText(
            text = body,
            style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
        )
    }
}

@Composable
private fun MainFeedErrorCard(
    message: String,
    colors: MainFeedColors,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outline, RoundedCornerShape(28.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BasicText(
            text = "Main Feed Failed To Load",
            style = ThulurTheme.Typography.headlineMedium.copy(color = colors.onSurface),
        )
        BasicText(
            text = message,
            style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
        )
        RetryButton(colors = colors, onRetry = onRetry)
    }
}

@Composable
private fun RetryButton(
    colors: MainFeedColors,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.accent)
            .clickable(onClick = onRetry)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        BasicText(
            text = "Retry",
            style = ThulurTheme.Typography.labelLarge.copy(color = colors.onAccent),
        )
    }
}

@Composable
private fun MainFeedSuccessContent(
    threads: List<MainFeedThread>,
    colors: MainFeedColors,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    text = "Main Feed",
                    style = ThulurTheme.Typography.displaySmall.copy(color = colors.onSurface),
                )
                BasicText(
                    text = "Technical vertical-slice rendering of daily_feed.",
                    style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
                )
            }
        }

        items(threads, key = { it.id }) { thread ->
            MainFeedThreadCard(thread = thread, colors = colors)
        }
    }
}

@Composable
private fun MainFeedThreadCard(
    thread: MainFeedThread,
    colors: MainFeedColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outline, RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BasicText(
                text = thread.name,
                style = ThulurTheme.Typography.headlineMedium.copy(color = colors.onSurface),
            )

            val topicLine = buildString {
                append("Topic: ")
                append(thread.topicName ?: "No topic")
                thread.firstSeen?.let {
                    append(" • First seen: ")
                    append(it.toString())
                }
            }

            BasicText(
                text = topicLine,
                style = ThulurTheme.Typography.labelLarge.copy(color = colors.accent),
            )

            thread.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                BasicText(
                    text = summary,
                    style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            thread.articles.forEach { article ->
                MainFeedArticleCard(article = article, colors = colors)
            }
        }
    }
}

@Composable
private fun MainFeedArticleCard(
    article: MainFeedArticle,
    colors: MainFeedColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceContainerHigh)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicText(
            text = article.title,
            style = ThulurTheme.Typography.titleLarge.copy(color = colors.onSurface),
        )

        article.displaySummary?.takeIf { it.isNotBlank() }?.let { summary ->
            BasicText(
                text = summary,
                style = ThulurTheme.Typography.bodyMedium.copy(color = colors.onSurfaceVariant),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QualityChip(quality = article.quality, colors = colors)
            BasicText(
                text = article.published ?: "No publish date",
                style = ThulurTheme.Typography.labelMedium.copy(color = colors.onSurfaceVariant),
            )
            BasicText(
                text = if (article.isRead) "Read" else "Unread",
                style = ThulurTheme.Typography.labelMedium.copy(color = colors.onSurfaceVariant),
            )
            if (article.isSuggestion) {
                BasicText(
                    text = "Suggested",
                    style = ThulurTheme.Typography.labelMedium.copy(color = colors.accent),
                )
            }
        }
    }
}

@Composable
private fun QualityChip(
    quality: MainFeedArticle.ArticleQuality,
    colors: MainFeedColors,
) {
    val (background, content) = when (quality) {
        MainFeedArticle.ArticleQuality.Trash -> colors.trashChipBackground to colors.trashChipContent
        MainFeedArticle.ArticleQuality.Default -> colors.defaultChipBackground to colors.defaultChipContent
        MainFeedArticle.ArticleQuality.Important -> colors.importantChipBackground to colors.importantChipContent
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        BasicText(
            text = quality.name,
            style = ThulurTheme.Typography.labelMedium.copy(color = content),
        )
    }
}

private data class MainFeedColors(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val accent: Color,
    val onAccent: Color,
    val trashChipBackground: Color,
    val trashChipContent: Color,
    val defaultChipBackground: Color,
    val defaultChipContent: Color,
    val importantChipBackground: Color,
    val importantChipContent: Color,
)

@Composable
@ReadOnlyComposable
private fun mainFeedColors(): MainFeedColors {
    val colors = ThulurTheme.Colors

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> MainFeedColors(
            surface = colors.slate.s50,
            surfaceContainer = colors.slate.s100,
            surfaceContainerHigh = colors.primary.s300A08,
            onSurface = colors.slate.s900,
            onSurfaceVariant = colors.slate.s700,
            outline = colors.slate.s300,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
            trashChipBackground = colors.error.s100,
            trashChipContent = colors.error.s700,
            defaultChipBackground = colors.slate.s300A10,
            defaultChipContent = colors.slate.s700,
            importantChipBackground = colors.primary.s100,
            importantChipContent = colors.primary.s700,
        )

        ThemeMode.Dark -> MainFeedColors(
            surface = colors.slate.s950,
            surfaceContainer = colors.slate.s900,
            surfaceContainerHigh = colors.slate.s300A08,
            onSurface = colors.slate.s50,
            onSurfaceVariant = colors.slate.s300,
            outline = colors.slate.s700,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
            trashChipBackground = colors.error.s900,
            trashChipContent = colors.error.s100,
            defaultChipBackground = colors.slate.s700,
            defaultChipContent = colors.slate.s50,
            importantChipBackground = colors.primary.s900,
            importantChipContent = colors.primary.s100,
        )
    }
}
