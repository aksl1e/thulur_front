package com.example.thulur.domain.usecase

import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Sends a chat message scoped to a single thread.
 */
class SendThreadChatMessageUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(threadId: String, message: String): String =
        thulurApiRepository.sendThreadChatMessage(
            threadId = threadId,
            message = message,
        )
}
