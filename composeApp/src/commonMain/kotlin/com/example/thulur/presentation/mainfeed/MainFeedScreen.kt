package com.example.thulur.presentation.mainfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurButtonContentDirection
import com.example.thulur.presentation.composables.ThulurChatFab
import com.example.thulur.presentation.composables.ThulurThreadItem
import com.example.thulur.presentation.composables.TopicsViewMode
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainFeedRoute(
    sessionInstanceId: Int,
    viewModel: MainFeedViewModel = koinViewModel(key = mainFeedViewModelKey(sessionInstanceId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainFeedScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onBackClick = viewModel::onBackClick,
        onForwardClick = viewModel::onForwardClick,
        onTopicsViewModeChange = viewModel::onTopicsViewModeChange,
        onThreadArticlesVisibilityToggle = viewModel::onThreadArticlesVisibilityToggle,
    )
}

internal fun mainFeedViewModelKey(sessionInstanceId: Int): String =
    "main-feed-session-$sessionInstanceId"

@Composable
fun MainFeedScreen(
    uiState: MainFeedUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onTopicsViewModeChange: (TopicsViewMode) -> Unit,
    onThreadArticlesVisibilityToggle: (String) -> Unit,
) {
    val colors = mainFeedColors()
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val title = uiState.selectedDay.toTitleAppBarLabel(today = today)
    val backLabel = uiState.selectedDay
        .minus(1, DateTimeUnit.DAY)
        .toNavigationAppBarLabel(today = today)
    val forwardDay = uiState.selectedDay.plus(1, DateTimeUnit.DAY)
    val leftRailWidth = 225.thulurDp()
    val contentStartPadding = 30.thulurDp()
    val fabBottomPadding = 30.thulurDp()
    val fabBottomInset = 110.thulurDp()
    val forwardLabel = if (uiState.selectedDay < today) {
        forwardDay.toNavigationAppBarLabel(today = today)
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeContentPadding()
    ) {
        ThulurAppBar(
            title = title,
            backLabel = backLabel,
            onBackClick = onBackClick,
            forwardLabel = forwardLabel,
            onForwardClick = onForwardClick.takeIf { forwardLabel != null },
            topicsViewMode = uiState.topicsViewMode,
            onTopicsViewModeChange = onTopicsViewModeChange,
            onSettingsClick = {},
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .width(leftRailWidth)
                    .fillMaxHeight()
                    .background(ThulurTheme.SemanticColors.appBar.containerColor),
            )

            when (val contentState = uiState.contentState) {
                MainFeedContentState.Loading -> MainFeedStatusCard(
                    title = "Loading Main Feed",
                    body = "Requesting daily_feed from Thulur API.",
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = leftRailWidth + contentStartPadding,
                            top = contentStartPadding,
                            end = contentStartPadding,
                            bottom = fabBottomInset,
                        ),
                )

                MainFeedContentState.Empty -> MainFeedStatusCard(
                    title = "Main Feed Is Empty",
                    body = "The backend returned an empty list. This is expected while the database is still empty.",
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = leftRailWidth + contentStartPadding,
                            top = contentStartPadding,
                            end = contentStartPadding,
                            bottom = fabBottomInset,
                        ),
                )

                is MainFeedContentState.Error -> MainFeedErrorCard(
                    message = contentState.message,
                    colors = colors,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = leftRailWidth + contentStartPadding,
                            top = contentStartPadding,
                            end = contentStartPadding,
                            bottom = fabBottomInset,
                        ),
                )

                is MainFeedContentState.Success -> MainFeedSuccessContent(
                    topicsViewMode = uiState.topicsViewMode,
                    articleVisibilityByThreadId = uiState.articleVisibilityByThreadId,
                    threads = contentState.threads,
                    onThreadArticlesVisibilityToggle = onThreadArticlesVisibilityToggle,
                    leadingLaneWidth = leftRailWidth,
                    contentStartPadding = contentStartPadding,
                    fabBottomInset = fabBottomInset,
                )
            }

            ThulurChatFab(
                text = "Discuss",
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = fabBottomPadding),
            )
        }
    }
}

@Composable
private fun MainFeedStatusCard(
    title: String,
    body: String,
    colors: MainFeedColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(colors.surfaceContainer, RoundedCornerShape(28.dp))
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(colors.surfaceContainer, RoundedCornerShape(28.dp))
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
            .background(colors.accent, RoundedCornerShape(999.dp))
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
    topicsViewMode: TopicsViewMode,
    articleVisibilityByThreadId: Map<String, Boolean>,
    threads: List<MainFeedThread>,
    onThreadArticlesVisibilityToggle: (String) -> Unit,
    leadingLaneWidth: androidx.compose.ui.unit.Dp,
    contentStartPadding: androidx.compose.ui.unit.Dp,
    fabBottomInset: androidx.compose.ui.unit.Dp,
) {
    val typography = ThulurTheme.SemanticTypography
    val moreArticlesColors = ThulurTheme.SemanticColors.threadItem.moreArticlesButton

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(
            top = contentStartPadding,
            bottom = fabBottomInset,
        ),
    ) {
        items(threads, key = { it.id }) { thread ->
            val areArticlesVisible = articleVisibilityByThreadId[thread.id]
                ?: topicsViewMode.defaultArticlesVisible()
            val moreArticlesLabel = thread.firstSeen?.toMoreArticlesDateLabel()

            ThulurThreadItem(
                title = thread.name,
                summary = thread.summary,
                onShowWholeSubjectClick = {},
                onToggleArticlesClick = { onThreadArticlesVisibilityToggle(thread.id) },
                areArticlesVisible = areArticlesVisible,
                articles = thread.articles.map { article -> article.toThulurThreadArticleData() },
                leadingLaneWidth = leadingLaneWidth,
                contentStartPadding = contentStartPadding,
                articlesLeadingContent = if (areArticlesVisible && moreArticlesLabel != null) {
                    {
                        ThulurButton(
                            text = "More articles",
                            supportingText = moreArticlesLabel,
                            onClick = {},
                            colorRole = ThulurColorRole.Slate,
                            useContainerStates = false,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowCircleLeft,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.thulurDp()),
                                )
                            },
                            contentDirection = ThulurButtonContentDirection.Vertical,
                            contentHorizontalAlignment = Alignment.End,
                            textStyle = typography.threadItemControl,
                            supportingTextStyle = typography.threadItemControl,
                            spacing = 5.thulurDp(),
                            stateColorsOverride = moreArticlesColors,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

private data class MainFeedColors(
    val surface: Color,
    val surfaceContainer: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val accent: Color,
    val onAccent: Color,
)

@Composable
@ReadOnlyComposable
private fun mainFeedColors(): MainFeedColors {
    val colors = ThulurTheme.Colors

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> MainFeedColors(
            surface = colors.slate.s50,
            surfaceContainer = colors.slate.s100,
            onSurface = colors.slate.s900,
            onSurfaceVariant = colors.slate.s700,
            outline = colors.slate.s300,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
        )

        ThemeMode.Dark -> MainFeedColors(
            surface = colors.slate.s950,
            surfaceContainer = colors.slate.s900,
            onSurface = colors.slate.s50,
            onSurfaceVariant = colors.slate.s300,
            outline = colors.slate.s700,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
        )
    }
}

private fun LocalDate.toTitleAppBarLabel(today: LocalDate): String {
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when (this) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> toShortDateLabel()
    }
}

private fun LocalDate.toNavigationAppBarLabel(today: LocalDate): String {
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when (this) {
        yesterday -> "Yesterday"
        today -> "Today"
        else -> toShortDateLabel()
    }
}

private fun LocalDate.toShortDateLabel(): String {
    val shortYear = (year % 100).toString().padStart(2, '0')
    val month = month.name
        .take(3)
        .lowercase()
        .replaceFirstChar(Char::uppercaseChar)
    val day = day.toString().padStart(2, '0')

    return "$day/$month/$shortYear"
}

private fun LocalDate.toMoreArticlesDateLabel(): String = toShortDateLabel()
