package com.example.thulur.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.presentation.auth.AuthRoute
import com.example.thulur.presentation.dailyfeed.DailyFeedRoute
import com.example.thulur.presentation.settings.SettingsRoute
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import org.koin.compose.viewmodel.koinViewModel
import com.example.thulur.presentation.chat.ChatRoute
@Composable
fun AppRootRoute(
    viewModel: AppRootViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val themeMode = when (val s = uiState) {
        is AppRootUiState.Ready -> s.themeMode
        is AppRootUiState.Unready -> s.themeMode
        AppRootUiState.Loading -> ThemeMode.Light
    }

    ThulurTheme(mode = themeMode) {
        when (val state = uiState) {
            AppRootUiState.Loading -> RootLoadingScreen()
            is AppRootUiState.Unready -> AuthRoute()
            is AppRootUiState.Ready -> when (state.destination) {
                AppRootAuthenticatedDestination.DailyFeed -> DailyFeedRoute(
                    sessionInstanceId = state.sessionInstanceId,
                    onOpenSettings = viewModel::openSettings,
                    onOpenChat = { threads -> viewModel.openChat(threads) },
                )

                AppRootAuthenticatedDestination.Settings -> SettingsRoute(
                    sessionInstanceId = state.sessionInstanceId,
                    onBackClick = viewModel::backToDailyFeed,
                    onThemeApplied = viewModel::updateTheme,
                )
                AppRootAuthenticatedDestination.Chat -> ChatRoute(
                    sessionInstanceId = state.sessionInstanceId,
                    threads = state.chatThreads,
                    onBackClick = viewModel::backToDailyFeed,
                )
            }
        }
    }
}
