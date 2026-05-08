package com.example.thulur.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.usecase.SendGeneralChatMessageUseCase
import com.example.thulur.domain.usecase.SendThreadChatMessageUseCase
import com.example.thulur.presentation.dailyfeed.OpenChat
import com.example.thulur.presentation.dailyfeed.OpenChatMode
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendGeneralChatMessageUseCase: SendGeneralChatMessageUseCase,
    private val sendThreadChatMessageUseCase: SendThreadChatMessageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var initializedOpenId: Int? = null

    fun initialize(openChat: OpenChat) {
        if (initializedOpenId == openChat.openId) return

        initializedOpenId = openChat.openId
        _uiState.value = ChatUiState(
            title = openChat.title,
            mode = openChat.mode,
        )
    }

    fun onInputValueChange(value: String) {
        if (_uiState.value.isSending) return
        _uiState.update { it.copy(inputValue = value) }
    }

    fun onSendClick() {
        val currentState = _uiState.value
        if (currentState.isSending) return

        val message = currentState.inputValue.trim()
        if (message.isBlank()) return

        _uiState.update { state ->
            state.copy(
                inputValue = "",
                isSending = true,
                messages = state.messages +
                    ChatMessage.User(text = message) +
                    ChatMessage.AssistantPending,
            )
        }

        viewModelScope.launch {
            val assistantMessage = try {
                ChatMessage.Assistant(
                    markdown = when (val mode = currentState.mode) {
                        OpenChatMode.General -> sendGeneralChatMessageUseCase(message = message)
                        is OpenChatMode.Thread -> sendThreadChatMessageUseCase(
                            threadId = mode.threadId,
                            message = message,
                        )
                    },
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                ChatMessage.Assistant(
                    markdown = throwable.message
                        ?.takeIf { it.isNotBlank() }
                        ?: DEFAULT_CHAT_ERROR_MESSAGE,
                    isError = true,
                )
            }

            _uiState.update { state ->
                state.copy(
                    isSending = false,
                    messages = state.messages.replacePendingAssistantWith(assistantMessage),
                )
            }
        }
    }
}

private fun List<ChatMessage>.replacePendingAssistantWith(
    replacement: ChatMessage.Assistant,
): List<ChatMessage> = if (lastOrNull() == ChatMessage.AssistantPending) {
    dropLast(1) + replacement
} else {
    this + replacement
}

private const val DEFAULT_CHAT_ERROR_MESSAGE: String =
    "Chat failed to respond. Please try again."
