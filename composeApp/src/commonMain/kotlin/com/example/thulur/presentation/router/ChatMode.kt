package com.example.thulur.presentation.router

sealed interface ChatMode {
    data object General : ChatMode

    data class Thread(val threadId: String) : ChatMode
}
