package com.example.thulur.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.theme.ThemeStore
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.presentation.theme.ThemeMode
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppRootViewModel(
    currentSessionProvider: CurrentSessionProvider,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val themeStore: ThemeStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AppRootUiState>(AppRootUiState.Loading)
    val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                currentSessionProvider.loadPersistedToken()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Throwable) {
                // Provider should swallow storage errors, but keep root resilient.
            }

            currentSessionProvider.sessionFlow
                .collect { session ->
                    if (session != null) {
                        val previousState = _uiState.value as? AppRootUiState.Ready
                        if (previousState?.sessionInstanceId != session.instanceId) {
                            _uiState.value = AppRootUiState.Loading
                            val theme = fetchTheme()
                            _uiState.value = AppRootUiState.Ready(
                                sessionInstanceId = session.instanceId,
                                themeMode = theme,
                            )
                        }
                    } else {
                        val cached = themeStore.readDarkMode()
                        _uiState.value = AppRootUiState.Unready(
                            themeMode = if (cached == true) ThemeMode.Dark else ThemeMode.Light,
                        )
                    }
                }
        }
    }

    fun openSettings() {
        updateAuthenticatedDestination(AppRootAuthenticatedDestination.Settings)
    }

    fun backToDailyFeed() {
        updateAuthenticatedDestination(AppRootAuthenticatedDestination.DailyFeed)
    }

    fun updateTheme(theme: ThemeMode) {
        val current = _uiState.value as? AppRootUiState.Ready ?: return
        if (current.themeMode == theme) return
        _uiState.value = current.copy(themeMode = theme)
        viewModelScope.launch { themeStore.writeDarkMode(theme == ThemeMode.Dark) }
    }

    private suspend fun fetchTheme(): ThemeMode {
        val cached = themeStore.readDarkMode()
        if (cached != null) return if (cached) ThemeMode.Dark else ThemeMode.Light
        return runCatching { getUserSettingsUseCase() }
            .map { settings ->
                themeStore.writeDarkMode(settings.darkMode)
                if (settings.darkMode) ThemeMode.Dark else ThemeMode.Light
            }
            .getOrElse { ThemeMode.Light }
    }

    private fun updateAuthenticatedDestination(destination: AppRootAuthenticatedDestination) {
        val currentState = _uiState.value as? AppRootUiState.Ready ?: return
        _uiState.value = currentState.copy(destination = destination)
    }
}
