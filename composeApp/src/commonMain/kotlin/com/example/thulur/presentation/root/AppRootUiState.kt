package com.example.thulur.presentation.root

import com.example.thulur.presentation.theme.ThemeMode

sealed interface AppRootUiState {
    data object Loading : AppRootUiState

    data class Unready(val themeMode: ThemeMode = ThemeMode.Light) : AppRootUiState

    data class Ready(
        val sessionInstanceId: Int,
        val themeMode: ThemeMode = ThemeMode.Light,
        val destination: AppRootAuthenticatedDestination = AppRootAuthenticatedDestination.MainFeed,
    ) : AppRootUiState
}

enum class AppRootAuthenticatedDestination {
    MainFeed,
    Settings,
    Chat,
}
