package com.example.thulur.presentation.root

sealed interface AppRootUiState {
    data object Loading : AppRootUiState

    data object Unauthenticated : AppRootUiState

    data class Authenticated(
        val sessionInstanceId: Int,
    ) : AppRootUiState
}
