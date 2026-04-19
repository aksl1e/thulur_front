package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads the current authenticated user.
 */
class GetCurrentUserUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(): CurrentUser = thulurApiRepository.getCurrentUser()
}
