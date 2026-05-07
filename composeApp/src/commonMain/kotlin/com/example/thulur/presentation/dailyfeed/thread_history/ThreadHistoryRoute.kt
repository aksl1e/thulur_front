package com.example.thulur.presentation.dailyfeed.thread_history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.thulur.domain.model.ThreadHistoryDay
import com.example.thulur.domain.usecase.GetThreadHistoryUseCase
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurChatFab
import com.example.thulur.presentation.composables.ThulurThreadArticleData
import com.example.thulur.presentation.composables.ThulurArticleItem
import com.example.thulur.presentation.composables.desktopHorizontalWheelScroll
import com.example.thulur.presentation.dailyfeed.DailyFeedErrorCard
import com.example.thulur.presentation.dailyfeed.DailyFeedStatusCard
import com.example.thulur.presentation.dailyfeed.OpenThreadHistory
import com.example.thulur.presentation.dailyfeed.dailyFeedColors
import com.example.thulur.presentation.dailyfeed.toThulurThreadArticleData
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurButtonStateSemanticColors
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.text.BasicText
import org.koin.compose.koinInject

@Composable
fun ThreadHistoryRoute(
    sessionInstanceId: Int,
    openThreadHistory: OpenThreadHistory,
    onBackClick: () -> Unit,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
) {
    val getThreadHistoryUseCase = koinInject<GetThreadHistoryUseCase>()
    val factory = remember(openThreadHistory, getThreadHistoryUseCase) {
        threadHistoryViewModelFactory(
            openThreadHistory = openThreadHistory,
            getThreadHistoryUseCase = getThreadHistoryUseCase,
        )
    }
    val viewModel: ThreadHistoryViewModel = viewModel(
        key = threadHistoryViewModelKey(
            sessionInstanceId = sessionInstanceId,
            threadId = openThreadHistory.threadId,
            initialDay = openThreadHistory.initialDay,
        ),
        factory = factory,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ThreadHistoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        onPreviousDayClick = viewModel::onPreviousDayClick,
        onNextDayClick = viewModel::onNextDayClick,
        onArticleClick = onArticleClick,
    )
}

internal fun threadHistoryViewModelKey(
    sessionInstanceId: Int,
    threadId: String,
    initialDay: LocalDate,
): String = "thread-history-session-$sessionInstanceId-thread-$threadId-day-$initialDay"

@Composable
private fun ThreadHistoryScreen(
    uiState: ThreadHistoryUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onPreviousDayClick: () -> Boolean,
    onNextDayClick: () -> Boolean,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
) {
    val colors = dailyFeedColors()
    val loadingColors = ThulurTheme.SemanticColors.rootLoadingScreen
    val leftRailWidth = 225.thulurDp()
    val contentStartPadding = 30.thulurDp()
    val bottomActionPadding = 30.thulurDp()
    val bottomActionInset = 110.thulurDp()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeContentPadding(),
    ) {
        ThulurAppBar(
            title = uiState.threadName,
            backLabel = "Back",
            onBackClick = onBackClick,
        )

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .width(leftRailWidth)
                    .fillMaxHeight()
                    .background(ThulurTheme.SemanticColors.appBar.containerColor),
            )

            when (val contentState = uiState.contentState) {
                ThreadHistoryContentState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = loadingColors.indicatorColor,
                    )
                }

                ThreadHistoryContentState.Empty -> DailyFeedStatusCard(
                    title = "Thread History Is Empty",
                    body = "The backend returned no history days for this subject.",
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = leftRailWidth + contentStartPadding,
                            top = contentStartPadding,
                            end = contentStartPadding,
                            bottom = bottomActionInset,
                        ),
                )

                is ThreadHistoryContentState.Error -> DailyFeedErrorCard(
                    title = "Thread History Failed To Load",
                    message = contentState.message,
                    colors = colors,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = leftRailWidth + contentStartPadding,
                            top = contentStartPadding,
                            end = contentStartPadding,
                            bottom = bottomActionInset,
                        ),
                )

                is ThreadHistoryContentState.Success -> ThreadHistorySuccessContent(
                    contentState = contentState,
                    onPreviousDayClick = onPreviousDayClick,
                    onNextDayClick = onNextDayClick,
                    onArticleClick = onArticleClick,
                    leadingLaneWidth = leftRailWidth,
                    contentStartPadding = contentStartPadding,
                    bottomActionInset = bottomActionInset,
                    bottomActionPadding = bottomActionPadding,
                )
            }

            if (uiState.contentState !is ThreadHistoryContentState.Success) {
                ThulurChatFab(
                    text = "Discuss",
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomActionPadding),
                )
            }
        }
    }
}

@Composable
private fun ThreadHistorySuccessContent(
    contentState: ThreadHistoryContentState.Success,
    onPreviousDayClick: () -> Boolean,
    onNextDayClick: () -> Boolean,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
    leadingLaneWidth: Dp,
    contentStartPadding: Dp,
    bottomActionInset: Dp,
    bottomActionPadding: Dp,
) {
    var isPageTransitionRunning by remember { mutableStateOf(false) }

    LaunchedEffect(contentState.visibleDayIndex) {
        if (!isPageTransitionRunning) return@LaunchedEffect

        delay(PAGE_FADE_DURATION_MS.toLong())
        isPageTransitionRunning = false
    }

    AnimatedContent(
        targetState = contentState.visibleDayIndex,
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = PAGE_FADE_DURATION_MS)) togetherWith
                fadeOut(animationSpec = tween(durationMillis = PAGE_FADE_DURATION_MS))
        },
        modifier = Modifier.fillMaxSize(),
    ) { visibleDayIndex ->
        Box(modifier = Modifier.fillMaxSize()) {
            ThreadHistoryDayPage(
                day = contentState.history.days[visibleDayIndex],
                onArticleClick = onArticleClick,
                leadingLaneWidth = leadingLaneWidth,
                contentStartPadding = contentStartPadding,
                bottomActionInset = bottomActionInset,
            )

            ThreadHistoryBottomActionRow(
                canGoToPreviousDay = contentState.canGoToPreviousDay && !isPageTransitionRunning,
                canGoToNextDay = contentState.canGoToNextDay && !isPageTransitionRunning,
                onPreviousDayClick = {
                    if (!isPageTransitionRunning && onPreviousDayClick()) {
                        isPageTransitionRunning = true
                    }
                },
                onNextDayClick = {
                    if (!isPageTransitionRunning && onNextDayClick()) {
                        isPageTransitionRunning = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomActionPadding),
            )
        }
    }
}

@Composable
private fun ThreadHistoryDayPage(
    day: ThreadHistoryDay,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
    leadingLaneWidth: Dp,
    contentStartPadding: Dp,
    bottomActionInset: Dp,
) {
    val colors = ThulurTheme.SemanticColors.threadItem
    val typography = ThulurTheme.SemanticTypography
    val articlesRowState = rememberLazyListState()
    val controlSpacing = 20.thulurDp()

    LaunchedEffect(day.day) {
        articlesRowState.scrollToItem(index = 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentStartPadding, bottom = bottomActionInset),
        verticalArrangement = Arrangement.spacedBy(20.thulurDp()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = leadingLaneWidth + contentStartPadding)
                .padding(end = 15.thulurDp()),
            verticalArrangement = Arrangement.spacedBy(20.thulurDp()),
        ) {
            BasicText(
                text = day.day.toThreadHistoryDayLabel(),
                style = typography.threadItemTitle.copy(color = colors.titleColor),
            )

            BasicText(
                text = day.threadSummary.orEmpty(),
                style = typography.threadItemSummary.copy(color = colors.summaryColor),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.thulurDp()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .width(leadingLaneWidth)
                    .fillMaxHeight(),
            )

            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .desktopHorizontalWheelScroll(articlesRowState),
                state = articlesRowState,
                contentPadding = PaddingValues(0.thulurDp()),
                horizontalArrangement = Arrangement.spacedBy(15.thulurDp()),
            ) {
                item {
                    Spacer(modifier = Modifier.width(controlSpacing))
                }
                items(
                    items = day.articles.map { article -> article.toThulurThreadArticleData() },
                    key = { article -> article.id },
                ) { article ->
                    ThulurArticleItem(
                        variant = article.variant,
                        title = article.title,
                        summary = article.summary,
                        sourceLabel = article.sourceLabel,
                        dateText = article.dateText,
                        timeText = article.timeText,
                        showDate = article.showDate,
                        imageUrl = article.imageUrl,
                        modifier = Modifier.height(340.thulurDp()),
                        onClick = { onArticleClick(article) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreadHistoryBottomActionRow(
    canGoToPreviousDay: Boolean,
    canGoToNextDay: Boolean,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.thulurDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThulurButton(
            onClick = onPreviousDayClick,
            enabled = canGoToPreviousDay,
            colorRole = ThulurColorRole.Slate,
            useContainerStates = false,
            shape = RoundedCornerShape(1000.thulurDp()),
            contentDescription = "Previous day",
            tooltipText = "Previous day",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowCircleLeft,
                    contentDescription = null,
                    modifier = Modifier.size(28.thulurDp()),
                )
            },
        )

        ThulurChatFab(
            text = "Discuss",
            onClick = {},
        )

        ThulurButton(
            onClick = onNextDayClick,
            enabled = canGoToNextDay,
            colorRole = ThulurColorRole.Slate,
            useContainerStates = false,
            shape = RoundedCornerShape(1000.thulurDp()),
            contentDescription = "Next day",
            tooltipText = "Next day",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowCircleRight,
                    contentDescription = null,
                    modifier = Modifier.size(28.thulurDp()),
                )
            },
        )
    }
}

internal fun LocalDate.toThreadHistoryDayLabel(): String {
    val day = day.toString().padStart(2, '0')
    val month = month.name
        .take(3)
        .lowercase()
        .replaceFirstChar(Char::uppercaseChar)

    return "$day/$month/$year"
}

private fun threadHistoryViewModelFactory(
    openThreadHistory: OpenThreadHistory,
    getThreadHistoryUseCase: GetThreadHistoryUseCase,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: kotlin.reflect.KClass<T>,
        extras: CreationExtras,
    ): T = ThreadHistoryViewModel(
        openThreadHistory = openThreadHistory,
        getThreadHistoryUseCase = getThreadHistoryUseCase,
    ) as T
}

private const val PAGE_FADE_DURATION_MS: Int = 220
