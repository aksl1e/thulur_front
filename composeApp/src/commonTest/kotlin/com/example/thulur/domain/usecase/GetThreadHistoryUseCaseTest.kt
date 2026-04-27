package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.ThreadHistoryDay
import com.example.thulur.domain.model.UserSettings
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

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

    override suspend fun getThreadHistory(threadId: String): ThreadHistory {
        requestedThreadId = threadId
        return history
    }

    override suspend fun getUserSettings(): UserSettings =
        error("Not used in this test")

    override suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings =
        error("Not used in this test")

    override suspend fun getFollowedFeeds(): List<Feed> =
        error("Not used in this test")

    override suspend fun getAllFeeds(): List<Feed> =
        error("Not used in this test")

    override suspend fun followFeed(identifier: String) =
        error("Not used in this test")

    override suspend fun unfollowFeed(feedId: String) =
        error("Not used in this test")

    override suspend fun getCurrentUser(): CurrentUser =
        error("Not used in this test")

    override suspend fun getAuthSessions(): List<AuthSession> =
        error("Not used in this test")

    override suspend fun terminateAuthSession(sessionId: String) =
        error("Not used in this test")

    override suspend fun rateArticle(articleId: String, rating: Int) =
        error("Not used in this test")
}
