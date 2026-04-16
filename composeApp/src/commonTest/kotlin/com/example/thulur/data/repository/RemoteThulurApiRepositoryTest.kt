package com.example.thulur.data.repository

import com.example.thulur.domain.model.MainFeedArticle
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.DailyFeedArticleDto
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.auth.AuthTokenDto
import com.example.thulur_api.dtos.auth.DesktopAuthMode
import com.example.thulur_api.dtos.auth.DesktopAuthStartDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RemoteThulurApiRepositoryTest {
    @Test
    fun `maps quality thresholds and sentinel first seen`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = listOf(
                    DailyFeedThreadDto(
                        threadId = "thread-1",
                        threadName = "Thread 1",
                        topicId = "topic-1",
                        topicName = "Topic 1",
                        mainFeedScore = 0.9,
                        threadFirstSeen = "9999-12-31",
                        threadSummary = "Summary",
                        articles = listOf(
                            article(score = 0.0, id = "trash"),
                            article(score = 0.33, id = "default"),
                            article(score = 0.66, id = "important"),
                        ),
                    ),
                ),
            ),
        )

        val thread = repository.getMainFeed().single()

        assertNull(thread.firstSeen)
        assertEquals(MainFeedArticle.ArticleQuality.Trash, thread.articles[0].quality)
        assertEquals(MainFeedArticle.ArticleQuality.Default, thread.articles[1].quality)
        assertEquals(MainFeedArticle.ArticleQuality.Important, thread.articles[2].quality)
    }

    @Test
    fun `parses regular first seen date`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = listOf(
                    DailyFeedThreadDto(
                        threadId = "thread-1",
                        threadName = "Thread 1",
                        topicId = null,
                        topicName = null,
                        mainFeedScore = 0.4,
                        threadFirstSeen = "2026-03-20",
                        threadSummary = null,
                        articles = listOf(article(score = 0.5)),
                    ),
                ),
            ),
        )

        val thread = repository.getMainFeed().single()

        assertEquals(LocalDate(2026, 3, 20), thread.firstSeen)
    }
}

private fun article(
    score: Double,
    id: String = "article",
) = DailyFeedArticleDto(
    articleId = id,
    feedId = "feed-1",
    title = "Title",
    url = "https://example.com/$id",
    published = null,
    qualityScore = score,
    novelty = false,
    noveltySummary = null,
    displaySummary = "Display summary",
    isRead = false,
    isSuggestion = false,
)

private class FakeThulurApi(
    private val threads: List<DailyFeedThreadDto>,
) : ThulurApi {
    override suspend fun getDailyFeed(
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = threads

    override suspend fun startDesktopAuth(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto = error("Not used in this test")

    override suspend fun exchangeAuthCode(
        code: String,
        state: String,
        deviceName: String?,
        platform: String?,
    ): AuthTokenDto =
        error("Not used in this test")
}
