package com.example.thulur.data.repository

import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.ArticleDto
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.ParagraphDto
import com.example.thulur_api.dtos.ThreadHistoryDayDto
import com.example.thulur_api.dtos.ThreadHistoryDto
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
        assertEquals(ArticleQuality.Trash, thread.articles[0].quality)
        assertEquals(ArticleQuality.Default, thread.articles[1].quality)
        assertEquals(ArticleQuality.Important, thread.articles[2].quality)
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

    @Test
    fun `maps article paragraphs into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = emptyList(),
                paragraphs = listOf(
                    ParagraphDto(idx = 0, text = "First paragraph", isNovel = true),
                    ParagraphDto(idx = 1, text = "Second paragraph", isNovel = false),
                ),
            ),
        )

        val paragraphs = repository.getArticleParagraphs(articleId = "article-1")

        assertEquals(
            listOf(
                ArticleParagraph(idx = 0, text = "First paragraph", isNovel = true),
                ArticleParagraph(idx = 1, text = "Second paragraph", isNovel = false),
            ),
            paragraphs,
        )
    }

    @Test
    fun `maps thread history into app facing model and ignores legacy novelty fields`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = emptyList(),
                history = ThreadHistoryDto(
                    threadId = "thread-1",
                    threadName = "Thread 1",
                    days = listOf(
                        ThreadHistoryDayDto(
                            day = "2026-04-17",
                            threadSummary = "Summary",
                            articles = listOf(
                                article(
                                    score = 0.9,
                                    id = "article-1",
                                    novelty = true,
                                    noveltySummary = "Legacy novelty summary",
                                    noveltyParagraphsIds = listOf("p-1", "p-2"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val history = repository.getThreadHistory(threadId = "thread-1")

        assertEquals("thread-1", history.threadId)
        assertEquals("Thread 1", history.threadName)
        assertEquals(LocalDate(2026, 4, 17), history.days.single().day)
        assertEquals("Summary", history.days.single().threadSummary)
        assertEquals(1, history.days.single().articles.size)
        assertEquals("article-1", history.days.single().articles.single().id)
        assertEquals(ArticleQuality.Important, history.days.single().articles.single().quality)
        assertEquals("Display summary", history.days.single().articles.single().displaySummary)
    }
}

private fun article(
    score: Double,
    id: String = "article",
    novelty: Boolean = false,
    noveltySummary: String? = null,
    noveltyParagraphsIds: List<String> = emptyList(),
) = ArticleDto(
    articleId = id,
    feedId = "feed-1",
    title = "Title",
    url = "https://example.com/$id",
    published = null,
    qualityScore = score,
    novelty = novelty,
    noveltySummary = noveltySummary,
    noveltyParagraphsIds = noveltyParagraphsIds,
    displaySummary = "Display summary",
    isRead = false,
    isSuggestion = false,
)

private class FakeThulurApi(
    private val threads: List<DailyFeedThreadDto>,
    private val paragraphs: List<ParagraphDto> = emptyList(),
    private val history: ThreadHistoryDto = ThreadHistoryDto(
        threadId = "thread-1",
        threadName = "Thread 1",
        days = emptyList(),
    ),
) : ThulurApi {
    override suspend fun getDailyFeed(
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = threads

    override suspend fun getArticleParagraphs(
        articleId: String,
    ): List<ParagraphDto> = paragraphs

    override suspend fun getThreadHistory(
        threadId: String,
    ): ThreadHistoryDto = history

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
