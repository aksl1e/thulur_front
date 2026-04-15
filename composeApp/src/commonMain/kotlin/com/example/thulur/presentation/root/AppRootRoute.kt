package com.example.thulur.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.presentation.auth.AuthRoute
import com.example.thulur.presentation.mainfeed.MainFeedRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRootRoute(
    viewModel: AppRootViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        AppRootUiState.Loading -> RootLoadingScreen()
        AppRootUiState.Unauthenticated -> AuthRoute()
        is AppRootUiState.Authenticated -> MainFeedRoute(sessionInstanceId = state.sessionInstanceId)
    }
}
