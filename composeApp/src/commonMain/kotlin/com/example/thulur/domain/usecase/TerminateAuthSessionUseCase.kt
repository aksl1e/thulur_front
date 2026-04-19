package com.example.thulur.domain.usecase

import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Terminates a single auth session.
 */
class TerminateAuthSessionUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(sessionId: String) {
        thulurApiRepository.terminateAuthSession(sessionId = sessionId)
    }
}
