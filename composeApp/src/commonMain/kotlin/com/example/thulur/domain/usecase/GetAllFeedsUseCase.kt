package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads all available feeds from the backend.
 */
class GetAllFeedsUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(): List<Feed> = thulurApiRepository.getAllFeeds()
}
