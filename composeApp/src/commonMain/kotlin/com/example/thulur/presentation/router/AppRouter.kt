package com.example.thulur.presentation.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.thulur.presentation.auth.AuthScreen as AuthContent
import com.example.thulur.presentation.auth.AuthViewModel
import com.example.thulur.presentation.chat.ChatScreen as ChatContent
import com.example.thulur.presentation.chat.ChatViewModel
import com.example.thulur.presentation.dailyfeed.DailyFeedScreen as DailyFeedContent
import com.example.thulur.presentation.dailyfeed.DailyFeedViewModel
import com.example.thulur.presentation.dailyfeed.dailyFeedColors
import com.example.thulur.presentation.dailyfeed.toTitleAppBarLabel
import com.example.thulur.presentation.dailyfeed.article_reader.ArticleReaderScreen as ArticleReaderContent
import com.example.thulur.presentation.dailyfeed.article_reader.ArticleReaderViewModel
import com.example.thulur.presentation.dailyfeed.thread_history.ThreadHistoryScreen as ThreadHistoryContent
import com.example.thulur.presentation.dailyfeed.thread_history.ThreadHistoryViewModel
import com.example.thulur.presentation.root.AppRootScreenModel
import com.example.thulur.presentation.root.AppRootUiState
import com.example.thulur.presentation.root.RootLoadingScreen
import com.example.thulur.presentation.root.canDiscussThread
import com.example.thulur.presentation.settings.SettingsScreen as SettingsContent
import com.example.thulur.presentation.settings.SettingsSnackBarEvent
import com.example.thulur.presentation.settings.SettingsViewModel
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import com.example.thulur.presentation.composables.ThulurSnackBar
import com.example.thulur.presentation.composables.ThulurSnackBarState
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.parameter.parametersOf

@Composable
fun AppRouter() {
    Navigator(screen = RouterLoadingScreen()) { navigator ->
        val rootScreenModel = navigator.rememberAppRootScreenModel()
        val rootState by rootScreenModel.uiState.collectAsState()
        val themeMode = rootState.themeMode()
        val navigationState = rootState.navigationState()

        LaunchedEffect(navigationState) {
            navigator.replaceAll(navigationState.toScreen())
        }

        ThulurTheme(mode = themeMode) {
            CurrentScreen()
        }
    }
}

private sealed interface AppNavigationState {
    data object Loading : AppNavigationState

    data object Unready : AppNavigationState

    data class Ready(val sessionInstanceId: Int) : AppNavigationState
}

private fun AppRootUiState.navigationState(): AppNavigationState = when (this) {
    AppRootUiState.Loading -> AppNavigationState.Loading
    is AppRootUiState.Unready -> AppNavigationState.Unready
    is AppRootUiState.Ready -> AppNavigationState.Ready(sessionInstanceId = sessionInstanceId)
}

private fun AppNavigationState.toScreen(): Screen = when (this) {
    AppNavigationState.Loading -> RouterLoadingScreen()
    AppNavigationState.Unready -> AuthScreen()
    is AppNavigationState.Ready -> DailyFeedScreen(sessionInstanceId = sessionInstanceId)
}

private fun AppRootUiState.themeMode(): ThemeMode = when (this) {
    AppRootUiState.Loading -> ThemeMode.Light
    is AppRootUiState.Unready -> themeMode
    is AppRootUiState.Ready -> themeMode
}

@Composable
private fun Navigator.rememberAppRootScreenModel(): AppRootScreenModel {
    return koinNavigatorScreenModel<AppRootScreenModel>()
}

private class RouterLoadingScreen : Screen {
    override val key: String = rootLoadingScreenKey()

    @Composable
    override fun Content() {
        RootLoadingScreen()
    }
}

class AuthScreen : Screen {
    override val key: String = authScreenKey()

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<AuthViewModel>()
        val uiState by screenModel.uiState.collectAsState()

        AuthContent(
            uiState = uiState,
            onEmailChange = screenModel::onEmailChange,
            onContinueClick = screenModel::onContinueClick,
            onTroubleSigningInClick = screenModel::onTroubleSigningInClick,
        )
    }
}

data class DailyFeedScreen(
    private val sessionInstanceId: Int,
) : Screen {
    override val key: String = dailyFeedScreenKey(sessionInstanceId = sessionInstanceId)

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<DailyFeedViewModel>()
        val uiState by screenModel.uiState.collectAsState()
        val colors = dailyFeedColors()

        DailyFeedContent(
            uiState = uiState,
            colors = colors,
            onRetry = screenModel::retry,
            onBackClick = screenModel::onBackClick,
            onForwardClick = screenModel::onForwardClick,
            onTopicsViewModeChange = screenModel::onTopicsViewModeChange,
            onThreadArticlesVisibilityToggle = screenModel::onThreadArticlesVisibilityToggle,
            onShowWholeSubjectClick = { threadId, threadName ->
                navigator.push(
                    ThreadHistoryScreen(
                        sessionInstanceId = sessionInstanceId,
                        threadId = threadId,
                        threadName = threadName,
                        initialDay = uiState.selectedDay,
                    ),
                )
            },
            onMoreArticlesClick = screenModel::onMoreArticlesClick,
            onArticleClick = { article ->
                navigator.push(
                    ArticleReaderScreen(
                        sessionInstanceId = sessionInstanceId,
                        articleId = article.id,
                        title = article.title,
                        url = article.url,
                        isRead = article.isRead,
                    ),
                )
            },
            onSettingsClick = {
                navigator.push(SettingsScreen(sessionInstanceId = sessionInstanceId))
            },
            onChatClick = {
                navigator.push(
                    ChatScreen(
                        sessionInstanceId = sessionInstanceId,
                        openId = nextChatOpenId(),
                        title = uiState.selectedDay.toTitleAppBarLabel(
                            today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        ),
                        mode = ChatMode.General,
                    ),
                )
            },
            onFeedScrollStateChange = screenModel::onFeedScrollStateChange,
            onFeedFocusConsumed = screenModel::onFeedFocusConsumed,
        )
    }
}

data class SettingsScreen(
    private val sessionInstanceId: Int,
) : Screen {
    override val key: String = settingsScreenKey(sessionInstanceId = sessionInstanceId)

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val rootScreenModel = navigator.rememberAppRootScreenModel()
        val screenModel = koinScreenModel<SettingsViewModel>()
        val uiState by screenModel.uiState.collectAsState()

        var prevTheme by remember { mutableStateOf(uiState.appState.values.theme) }
        val currentTheme = uiState.appState.values.theme
        SideEffect {
            if (prevTheme != currentTheme) {
                rootScreenModel.updateTheme(currentTheme)
                prevTheme = currentTheme
            }
        }

        var snackBarId by remember { mutableLongStateOf(0L) }
        var snackBarMessage by remember { mutableStateOf("") }
        var showSnackBar by remember { mutableStateOf(false) }

        LaunchedEffect(screenModel) {
            screenModel.snackBarEvents.collect { event ->
                snackBarId++
                showSnackBar = true
                when (event) {
                    is SettingsSnackBarEvent.Error -> snackBarMessage = event.message
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            SettingsContent(
                uiState = uiState,
                onBackClick = { navigator.pop() },
                onRetryLoad = screenModel::retryLoad,
                onSectionSelected = screenModel::onSectionSelected,
                onTerminateSessionClick = screenModel::onTerminateSessionClick,
                onRetryFeedsLoad = screenModel::retryFeedsLoad,
                onFeedSearchQueryChanged = screenModel::onFeedSearchQueryChanged,
                onFollowFeedClick = screenModel::onFollowFeedClick,
                onUnfollowFeedClick = screenModel::onUnfollowFeedClick,
                onThemeSelected = screenModel::onThemeSelected,
                onLanguageSelected = screenModel::onLanguageSelected,
                onNotificationsEnabledChanged = screenModel::onNotificationsEnabledChanged,
                onSuggestionsOutsideChanged = screenModel::onSuggestionsOutsideChanged,
                onFeedScheduleChanged = screenModel::onFeedScheduleChanged,
            )

            if (showSnackBar) {
                key(snackBarId) {
                    ThulurSnackBar(
                        message = snackBarMessage,
                        state = ThulurSnackBarState.Error,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.thulurDp(), vertical = 24.thulurDp()),
                    )
                }
            }
        }
    }
}

data class ThreadHistoryScreen(
    private val sessionInstanceId: Int,
    private val threadId: String,
    private val threadName: String,
    private val initialDay: kotlinx.datetime.LocalDate,
) : Screen {
    override val key: String = threadHistoryScreenKey(
        sessionInstanceId = sessionInstanceId,
        threadId = threadId,
        initialDay = initialDay,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val rootScreenModel = navigator.rememberAppRootScreenModel()
        val rootState by rootScreenModel.uiState.collectAsState()
        val canDiscussThread = (rootState as? AppRootUiState.Ready)?.subscriptionTier?.canDiscussThread() == true
        val screenModel = koinScreenModel<ThreadHistoryViewModel>(
            parameters = { parametersOf(threadId, threadName, initialDay) },
        )
        val uiState by screenModel.uiState.collectAsState()

        ThreadHistoryContent(
            uiState = uiState,
            canDiscussThread = canDiscussThread,
            onBackClick = { navigator.pop() },
            onRetry = screenModel::retry,
            onPreviousDayClick = screenModel::onPreviousDayClick,
            onNextDayClick = screenModel::onNextDayClick,
            onArticleClick = { article ->
                navigator.push(
                    ArticleReaderScreen(
                        sessionInstanceId = sessionInstanceId,
                        articleId = article.id,
                        title = article.title,
                        url = article.url,
                        isRead = article.isRead,
                    ),
                )
            },
            onDiscussClick = {
                if (canDiscussThread) {
                    navigator.push(
                        ChatScreen(
                            sessionInstanceId = sessionInstanceId,
                            openId = nextChatOpenId(),
                            title = threadName,
                            mode = ChatMode.Thread(threadId = threadId),
                        ),
                    )
                }
            },
        )
    }
}

data class ChatScreen(
    private val sessionInstanceId: Int,
    private val openId: Int,
    private val title: String,
    private val mode: ChatMode,
) : Screen {
    override val key: String = chatScreenKey(
        sessionInstanceId = sessionInstanceId,
        openId = openId,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ChatViewModel>(
            parameters = { parametersOf(title, mode) },
        )
        val uiState by screenModel.uiState.collectAsState()

        ChatContent(
            uiState = uiState,
            onBackClick = { navigator.pop() },
            onInputValueChange = screenModel::onInputValueChange,
            onSendClick = screenModel::onSendClick,
        )
    }
}

data class ArticleReaderScreen(
    private val sessionInstanceId: Int,
    private val articleId: String,
    private val title: String,
    private val url: String,
    private val isRead: Boolean,
) : Screen {
    override val key: String = articleReaderScreenKey(
        sessionInstanceId = sessionInstanceId,
        articleId = articleId,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ArticleReaderViewModel>(
            parameters = { parametersOf(articleId, title, url, isRead) },
        )
        val uiState by screenModel.uiState.collectAsState()

        ArticleReaderContent(
            uiState = uiState,
            onBackClick = {
                screenModel.screenModelScope.launch {
                    screenModel.submitRate()
                    navigator.pop()
                }
            },
            onInitialPageLoaded = screenModel::onInitialPageLoaded,
            onInjectionSucceeded = screenModel::onInjectionSucceeded,
            onProgressChanged = screenModel::onProgressChanged,
            onRateArticle = screenModel::onRateArticle,
            onError = screenModel::onBrowserError,
        )
    }
}

private var nextChatScreenOpenId: Int = 0

private fun nextChatOpenId(): Int = nextChatScreenOpenId++
