package com.example.thulur.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class ChatViewModel(
    private val getMainFeedUseCase: GetMainFeedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadThreads()
    }

    fun onThreadClick(thread: MainFeedThread) {
        _uiState.update { it.copy(selectedThreadId = thread.id) }
    }

    private fun loadThreads() {
        viewModelScope.launch {
            _uiState.update { it.copy(contentState = ChatContentState.Loading) }

            val contentState = try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val threads = getMainFeedUseCase(day = today)
                if (threads.isEmpty()) {
                    ChatContentState.Empty
                } else {
                    ChatContentState.Success(threads)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                ChatContentState.Error(
                    message = throwable.message ?: "Failed to load threads.",
                )
            }

            _uiState.update { state ->
                state.copy(
                    contentState = contentState,
                    selectedThreadId = (contentState as? ChatContentState.Success)?.threads?.firstOrNull()?.id,
                )
            }
        }
    }
}