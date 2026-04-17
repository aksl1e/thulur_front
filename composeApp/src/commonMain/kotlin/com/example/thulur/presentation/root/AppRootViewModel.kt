package com.example.thulur.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.session.CurrentSessionProvider
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppRootViewModel(
    currentSessionProvider: CurrentSessionProvider,
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
                    _uiState.value = if (session != null) {
                        AppRootUiState.Authenticated(sessionInstanceId = session.instanceId)
                    } else {
                        AppRootUiState.Unauthenticated
                    }
                }
        }
    }
}
