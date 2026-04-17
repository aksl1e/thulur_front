package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.repository.ThulurApiRepository

/**
 * Loads paragraph metadata for a single article.
 */
class GetArticleParagraphsUseCase(
    private val thulurApiRepository: ThulurApiRepository,
) {
    suspend operator fun invoke(articleId: String): List<ArticleParagraph> =
        thulurApiRepository.getArticleParagraphs(articleId = articleId)
}
