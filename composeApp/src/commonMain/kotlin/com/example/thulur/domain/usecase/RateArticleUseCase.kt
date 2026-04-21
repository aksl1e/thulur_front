package com.example.thulur.domain.usecase

import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Submits a reading-quality rating for a single article.
 */
class RateArticleUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(articleId: String, rating: Int) {
        thulurApiRepository.rateArticle(articleId = articleId, rating = rating)
    }
}
