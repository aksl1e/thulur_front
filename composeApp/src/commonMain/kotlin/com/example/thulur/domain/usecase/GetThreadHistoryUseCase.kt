package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads thread history for a single thread.
 */
class GetThreadHistoryUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(threadId: String): ThreadHistory =
        thulurApiRepository.getThreadHistory(threadId = threadId)
}
