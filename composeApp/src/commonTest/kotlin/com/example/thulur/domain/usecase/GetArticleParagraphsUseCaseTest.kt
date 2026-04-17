package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.repository.ThulurApiRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class GetArticleParagraphsUseCaseTest {
    @Test
    fun `delegates to repository without changing paragraph data`() = runTest {
        val repository = TrackingRepository(
            paragraphs = listOf(
                ArticleParagraph(idx = 0, text = "First", isNovel = true),
                ArticleParagraph(idx = 1, text = "Second", isNovel = false),
            ),
        )

        val response = GetArticleParagraphsUseCase(repository)(articleId = "article-1")

        assertEquals("article-1", repository.requestedArticleId)
        assertEquals(
            listOf(
                ArticleParagraph(idx = 0, text = "First", isNovel = true),
                ArticleParagraph(idx = 1, text = "Second", isNovel = false),
            ),
            response,
        )
    }
}

private class TrackingRepository(
    private val paragraphs: List<ArticleParagraph>,
) : ThulurApiRepository {
    var requestedArticleId: String? = null

    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> {
        requestedArticleId = articleId
        return paragraphs
    }
}
