package com.example.thulur.domain.usecase

import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Sends a general chat message for the current user feed chat.
 */
class SendGeneralChatMessageUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(message: String): String =
        thulurApiRepository.sendGeneralChatMessage(message = message)
}
