package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads feeds followed by the current user.
 */
class GetFollowedFeedsUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(): List<Feed> = thulurApiRepository.getFollowedFeeds()
}
