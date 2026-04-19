package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads active auth sessions for the current user.
 */
class GetAuthSessionsUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(): List<AuthSession> = thulurApiRepository.getAuthSessions()
}
