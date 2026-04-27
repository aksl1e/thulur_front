package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.repository.ThulurApiRepository
import kotlinx.datetime.LocalDate

/**
 * Loads the Daily Feed used by the Daily Feed feature.
 */
class GetDailyFeedUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(day: LocalDate? = null): DailyFeed =
        thulurApiRepository.getDailyFeed(day = day)
}
