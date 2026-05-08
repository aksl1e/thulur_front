package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class ChatUseCaseTest {
    @Test
    fun `general chat use case delegates message and returns response unchanged`() = runTest {
        val repository = TrackingChatRepository(
            generalChatResponse = "Feed reply",
            threadChatResponse = "Thread reply",
        )

        val response = SendGeneralChatMessageUseCase(repository)(message = "Hello feed")

        assertEquals("Hello feed", repository.generalChatMessage)
        assertEquals("Feed reply", response)
    }

    @Test
    fun `thread chat use case delegates thread id and message and returns response unchanged`() = runTest {
        val repository = TrackingChatRepository(
            generalChatResponse = "Feed reply",
            threadChatResponse = "Thread reply",
        )

        val response = SendThreadChatMessageUseCase(repository)(
            threadId = "thread-1",
            message = "Hello thread",
        )

        assertEquals("thread-1", repository.threadChatThreadId)
        assertEquals("Hello thread", repository.threadChatMessage)
        assertEquals("Thread reply", response)
    }
}

private class TrackingChatRepository(
    private val generalChatResponse: String,
    private val threadChatResponse: String,
) : ThulurApiRepository {
    var generalChatMessage: String? = null
    var threadChatThreadId: String? = null
    var threadChatMessage: String? = null

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

    override suspend fun getThreadHistory(threadId: String): ThreadHistory =
        error("Not used in this test")

    override suspend fun sendGeneralChatMessage(message: String): String {
        generalChatMessage = message
        return generalChatResponse
    }

    override suspend fun sendThreadChatMessage(threadId: String, message: String): String {
        threadChatThreadId = threadId
        threadChatMessage = message
        return threadChatResponse
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
