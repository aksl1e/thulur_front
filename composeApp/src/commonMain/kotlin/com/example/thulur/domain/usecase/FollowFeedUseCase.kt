package com.example.thulur.domain.usecase

import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Follows a single feed for the current user.
 */
class FollowFeedUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(feedId: String) {
        thulurApiRepository.followFeed(feedId = feedId)
    }
}
