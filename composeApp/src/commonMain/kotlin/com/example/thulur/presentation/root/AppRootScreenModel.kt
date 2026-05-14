package com.example.thulur.presentation.root

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.theme.ThemeStore
import com.example.thulur.domain.usecase.GetCurrentUserUseCase
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.presentation.theme.ThemeMode
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppRootScreenModel(
    currentSessionProvider: CurrentSessionProvider,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val themeStore: ThemeStore,
) : ScreenModel {
    private val _uiState = MutableStateFlow<AppRootUiState>(AppRootUiState.Loading)
    val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()
    private var subscriptionLoadJob: Job? = null

    init {
        screenModelScope.launch {
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
                            subscriptionLoadJob?.cancel()
                            _uiState.value = AppRootUiState.Loading
                            val theme = fetchTheme()
                            _uiState.value = AppRootUiState.Ready(
                                sessionInstanceId = session.instanceId,
                                themeMode = theme,
                            )
                            startSubscriptionLoad(session.instanceId)
                        }
                    } else {
                        subscriptionLoadJob?.cancel()
                        val cached = themeStore.readDarkMode()
                        _uiState.value = AppRootUiState.Unready(
                            themeMode = if (cached == true) ThemeMode.Dark else ThemeMode.Light,
                        )
                    }
                }
        }
    }

    fun updateTheme(theme: ThemeMode) {
        val current = _uiState.value as? AppRootUiState.Ready ?: return
        if (current.themeMode == theme) return
        _uiState.value = current.copy(themeMode = theme)
        screenModelScope.launch { themeStore.writeDarkMode(theme == ThemeMode.Dark) }
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

    private fun startSubscriptionLoad(sessionInstanceId: Int) {
        subscriptionLoadJob?.cancel()
        subscriptionLoadJob = screenModelScope.launch {
            if (loadSubscriptionTier(sessionInstanceId)) return@launch

            for (delayMs in SUBSCRIPTION_RETRY_DELAYS_MS) {
                delay(delayMs)
                if (loadSubscriptionTier(sessionInstanceId)) return@launch
            }
        }
    }

    private suspend fun loadSubscriptionTier(sessionInstanceId: Int): Boolean = try {
        val tier = getCurrentUserUseCase().subscriptionTier.toAppSubscriptionTier()
        val currentState = _uiState.value as? AppRootUiState.Ready ?: return true
        if (currentState.sessionInstanceId != sessionInstanceId) return true

        _uiState.value = currentState.copy(subscriptionTier = tier)
        true
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Throwable) {
        false
    }
}

private val SUBSCRIPTION_RETRY_DELAYS_MS = listOf(5_000L, 15_000L, 30_000L, 60_000L)
