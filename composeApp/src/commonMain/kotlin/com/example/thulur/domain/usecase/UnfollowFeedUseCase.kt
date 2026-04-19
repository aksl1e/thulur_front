package com.example.thulur.domain.usecase

import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Unfollows a single feed for the current user.
 */
class UnfollowFeedUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(feedId: String) {
        thulurApiRepository.unfollowFeed(feedId = feedId)
    }
}
