package com.example.thulur.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.session.CurrentSessionProvider
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppRootViewModel(
    currentSessionProvider: CurrentSessionProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppRootUiState.Loading)
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

            currentSessionProvider.tokenFlow
                .map { token ->
                    if (token.isNullOrBlank()) {
                        AppRootUiState.Unauthenticated
                    } else {
                        AppRootUiState.Authenticated
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }
}
