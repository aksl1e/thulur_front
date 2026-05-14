package com.example.thulur.presentation.dailyfeed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.material.Icon
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.tween
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.presentation.composables.DesktopScrollCoordinator
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurButtonContentDirection
import com.example.thulur.presentation.composables.ThulurChatFab
import com.example.thulur.presentation.composables.ThulurThreadArticleData
import com.example.thulur.presentation.composables.ThulurThreadItem
import com.example.thulur.presentation.composables.TopicsViewMode
import com.example.thulur.presentation.composables.TopicsSwitch
import com.example.thulur.presentation.composables.desktopScrollRootObserver
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
fun DailyFeedScreen(
    uiState: DailyFeedUiState,
    colors: DailyFeedColors,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChatClick: () -> Unit,
    onTopicsViewModeChange: (TopicsViewMode) -> Unit,
    onThreadArticlesVisibilityToggle: (String) -> Unit,
    onShowWholeSubjectClick: (String, String) -> Unit,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
    onFeedScrollStateChange: (Int, Int) -> Unit = { _, _ -> },
) {
    val appBarColors = ThulurTheme.SemanticColors.appBar
    val semanticTypography = ThulurTheme.SemanticTypography
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
            forwardButton = if (forwardLabel != null) {
                {
                    ThulurButton(
                        text = forwardLabel,
                        onClick = onForwardClick,
                        colorRole = ThulurColorRole.Slate,
                        useContainerStates = false,
                        stateColorsOverride = appBarColors.forwardButton,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ArrowCircleRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.thulurDp()),
                            )
                        },
                        textStyle = semanticTypography.appBarBackLabel,
                        contentPadding = PaddingValues(horizontal = 10.thulurDp()),
                        spacing = 10.thulurDp(),
                    )
                }
            } else {
                null
            },
            endPrimaryContent = {
                TopicsSwitch(
                    selected = uiState.topicsViewMode,
                    onSelect = onTopicsViewModeChange,
                )
            },
            endSecondaryContent = {
                ThulurButton(
                    onClick = onSettingsClick,
                    colorRole = ThulurColorRole.Slate,
                    useContainerStates = false,
                    stateColorsOverride = appBarColors.settingsButton,
                    contentPadding = PaddingValues(),
                    contentDescription = "Settings",
                    tooltipText = "Settings",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(32.thulurDp()),
                        )
                    },
                )
            },
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
                DailyFeedContentState.Loading -> CircularProgressIndicator(
                    color = colors.surface,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(
                            start = leftRailWidth + contentStartPadding,
                            top = contentStartPadding,
                            end = contentStartPadding,
                            bottom = fabBottomInset,
                        ),
                )

                DailyFeedContentState.Empty -> DailyFeedStatusCard(
                    title = "Daily Feed Is Empty",
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

                is DailyFeedContentState.Error -> DailyFeedErrorCard(
                    title = "Daily Feed Failed To Load",
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

                is DailyFeedContentState.Success -> DailyFeedSuccessContent(
                    topicsViewMode = uiState.topicsViewMode,
                    articleVisibilityByThreadId = uiState.articleVisibilityByThreadId,
                    threads = contentState.threads,
                    onThreadArticlesVisibilityToggle = onThreadArticlesVisibilityToggle,
                    onShowWholeSubjectClick = onShowWholeSubjectClick,
                    onArticleClick = onArticleClick,
                    leadingLaneWidth = leftRailWidth,
                    contentStartPadding = contentStartPadding,
                    fabBottomInset = fabBottomInset,
                    scrollIndex = uiState.feedScrollIndex,
                    scrollOffset = uiState.feedScrollOffset,
                    onScrollStateChange = onFeedScrollStateChange,
                )
            }

            ThulurChatFab(
                text = "Discuss",
                onClick = onChatClick, // Change the screen here
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = fabBottomPadding),
            )
        }
    }
}

@Composable
private fun DailyFeedSuccessContent(
    topicsViewMode: TopicsViewMode,
    articleVisibilityByThreadId: Map<String, Boolean>,
    threads: List<DailyFeedThread>,
    onThreadArticlesVisibilityToggle: (String) -> Unit,
    onShowWholeSubjectClick: (String, String) -> Unit,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
    leadingLaneWidth: androidx.compose.ui.unit.Dp,
    contentStartPadding: androidx.compose.ui.unit.Dp,
    fabBottomInset: androidx.compose.ui.unit.Dp,
    scrollIndex: Int = 0,
    scrollOffset: Int = 0,
    onScrollStateChange: (Int, Int) -> Unit = { _, _ -> },
) {
    val typography = ThulurTheme.SemanticTypography
    val moreArticlesColors = ThulurTheme.SemanticColors.threadItem.moreArticlesButton
    val desktopScrollCoordinator = remember { DesktopScrollCoordinator() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollIndex,
        initialFirstVisibleItemScrollOffset = scrollOffset,
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) -> onScrollStateChange(index, offset) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .desktopScrollRootObserver(desktopScrollCoordinator),
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
                threadId = thread.id,
                title = thread.name,
                summary = thread.summary,
                onShowWholeSubjectClick = {
                    onShowWholeSubjectClick(thread.id, thread.name)
                },
                onToggleArticlesClick = { onThreadArticlesVisibilityToggle(thread.id) },
                onArticleClick = onArticleClick,
                areArticlesVisible = areArticlesVisible,
                articles = thread.articles.map { article -> article.toThulurThreadArticleData() },
                leadingLaneWidth = leadingLaneWidth,
                contentStartPadding = contentStartPadding,
                desktopScrollCoordinator = desktopScrollCoordinator,
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

internal fun LocalDate.toTitleAppBarLabel(today: LocalDate): String {
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when (this) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> toShortDateLabel()
    }
}

internal fun LocalDate.toNavigationAppBarLabel(today: LocalDate): String {
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when (this) {
        yesterday -> "Yesterday"
        today -> "Today"
        else -> toShortDateLabel()
    }
}

internal fun LocalDate.toShortDateLabel(): String {
    val shortYear = (year % 100).toString().padStart(2, '0')
    val month = month.name
        .take(3)
        .lowercase()
        .replaceFirstChar(Char::uppercaseChar)
    val day = day.toString().padStart(2, '0')

    return "$day/$month/$shortYear"
}

private fun LocalDate.toMoreArticlesDateLabel(): String = toShortDateLabel()
