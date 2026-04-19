package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads settings for the current user.
 */
class GetUserSettingsUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(): UserSettings = thulurApiRepository.getUserSettings()
}
