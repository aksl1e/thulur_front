package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.repository.ThulurApiRepository
import kotlinx.datetime.LocalDate

/**
 * Loads the Main Feed threads used by the Main Feed feature.
 */
class GetMainFeedUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(day: LocalDate? = null): List<MainFeedThread> =
        thulurApiRepository.getMainFeed(day = day)
}
