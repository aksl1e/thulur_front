package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Applies a partial settings update for the current user.
 */
class PatchUserSettingsUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(update: PatchUserSettings): UserSettings =
        thulurApiRepository.patchUserSettings(patch = update)
}
