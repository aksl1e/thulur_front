package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.ThreadHistoryDay
import com.example.thulur.domain.repository.ThulurApiRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class GetThreadHistoryUseCaseTest {
    @Test
    fun `delegates thread id to repository and returns response unchanged`() = runTest {
        val history = ThreadHistory(
            threadId = "thread-1",
            threadName = "Thread 1",
            days = listOf(
                ThreadHistoryDay(
                    day = LocalDate(2026, 4, 17),
                    threadSummary = "Summary",
                    articles = listOf(
                        Article(
                            id = "article-1",
                            feedId = "feed-1",
                            title = "Article",
                            url = "https://example.com/article-1",
                            published = "2026-04-17T08:00:00",
                            displaySummary = "Visible summary",
                            isRead = false,
                            isSuggestion = true,
                            quality = ArticleQuality.Important,
                        ),
                    ),
                ),
            ),
        )
        val repository = TrackingThreadHistoryRepository(history = history)

        val response = GetThreadHistoryUseCase(repository)(threadId = "thread-1")

        assertEquals("thread-1", repository.requestedThreadId)
        assertEquals(history, response)
    }
}

private class TrackingThreadHistoryRepository(
    private val history: ThreadHistory,
) : ThulurApiRepository {
    var requestedThreadId: String? = null

    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

    override suspend fun getThreadHistory(threadId: String): ThreadHistory {
        requestedThreadId = threadId
        return history
    }
}
