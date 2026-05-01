package com.example.thulur.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.usecase.GetDailyFeedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initWithThreads(threads: List<DailyFeedThread>) {
        if (_uiState.value.contentState != ChatContentState.Loading) return // already initialized
        val contentState = if (threads.isEmpty()) {
            ChatContentState.Empty
        } else {
            ChatContentState.Success(threads)
        }
        _uiState.update {
            it.copy(
                contentState = contentState,
                selectedThreadId = (contentState as? ChatContentState.Success)?.threads?.firstOrNull()?.id,
            )
        }
    }

    fun onThreadClick(thread: DailyFeedThread) {
        _uiState.update { it.copy(selectedThreadId = thread.id) }
    }

    fun onInputValueChange(value: String) {
        _uiState.update { it.copy(inputValue = value) }
    }

    fun onSendClick() {
        val text = _uiState.value.inputValue.trim()
        if (text.isBlank()) return
        _uiState.update {
            it.copy(
                messages = it.messages + Message(text = text, isUser = true),
                inputValue = "",
            )
        }
    }
}