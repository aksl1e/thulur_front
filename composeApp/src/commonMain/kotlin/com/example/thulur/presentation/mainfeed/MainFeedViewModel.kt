package com.example.thulur.presentation.mainfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Shared Compose ViewModel driving the technical Main Feed screen.
 */
class MainFeedViewModel(
    private val getMainFeedUseCase: GetMainFeedUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MainFeedUiState>(MainFeedUiState.Loading)
    val uiState: StateFlow<MainFeedUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Reloads the Main Feed.
     */
    fun refresh(day: LocalDate? = null) {
        viewModelScope.launch {
            _uiState.value = MainFeedUiState.Loading

            _uiState.value = runCatching {
                getMainFeedUseCase(day = day)
            }.fold(
                onSuccess = { threads ->
                    if (threads.isEmpty()) {
                        MainFeedUiState.Empty
                    } else {
                        MainFeedUiState.Success(threads)
                    }
                },
                onFailure = { throwable ->
                    MainFeedUiState.Error(
                        message = throwable.message ?: "Failed to load Main Feed.",
                    )
                },
            )
        }
    }
}
