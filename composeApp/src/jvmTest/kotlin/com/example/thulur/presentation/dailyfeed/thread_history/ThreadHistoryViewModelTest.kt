package com.example.thulur.presentation.dailyfeed.thread_history

import androidx.lifecycle.ViewModel
import com.example.thulur.data.session.InMemoryReadArticlesCache
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
import com.example.thulur.domain.usecase.GetThreadHistoryUseCase
import com.example.thulur.presentation.dailyfeed.OpenThreadHistory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ThreadHistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads requested thread history and opens exact initial day when present`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(date = LocalDate(2026, 4, 13), summary = "13 Apr", articleId = "article-13"),
                        day(date = LocalDate(2026, 4, 14), summary = "14 Apr", articleId = "article-14"),
                        day(date = LocalDate(2026, 4, 15), summary = "15 Apr", articleId = "article-15"),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 14),
        )

        advanceUntilIdle()

        val success = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState)
        assertEquals("thread-1", repository.requestedThreadId)
        assertEquals(LocalDate(2026, 4, 14), success.visibleDay().day)
        assertEquals("14 Apr", success.visibleDay().threadSummary)
    }

    @Test
    fun `falls back to nearest older day when exact initial day is absent`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(date = LocalDate(2026, 4, 18), summary = "18 Apr", articleId = "article-18"),
                        day(date = LocalDate(2026, 4, 16), summary = "16 Apr", articleId = "article-16"),
                        day(date = LocalDate(2026, 4, 13), summary = "13 Apr", articleId = "article-13"),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 14),
        )

        advanceUntilIdle()

        val success = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState)
        assertEquals(LocalDate(2026, 4, 13), success.visibleDay().day)
    }

    @Test
    fun `falls back to newest available day when there is no older day`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(date = LocalDate(2026, 4, 18), summary = "18 Apr", articleId = "article-18"),
                        day(date = LocalDate(2026, 4, 16), summary = "16 Apr", articleId = "article-16"),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 14),
        )

        advanceUntilIdle()

        val success = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState)
        assertEquals(LocalDate(2026, 4, 18), success.visibleDay().day)
    }

    @Test
    fun `paging swaps both summary and articles together`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(date = LocalDate(2026, 4, 17), summary = "17 Apr summary", articleId = "article-17"),
                        day(date = LocalDate(2026, 4, 16), summary = "16 Apr summary", articleId = "article-16"),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 17),
        )

        advanceUntilIdle()
        assertTrue(viewModel.onPreviousDayClick())

        val olderDay = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState).visibleDay()
        assertEquals("16 Apr summary", olderDay.threadSummary)
        assertEquals("article-16", olderDay.articles.single().id)

        assertTrue(viewModel.onNextDayClick())
        val newerDay = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState).visibleDay()
        assertEquals("17 Apr summary", newerDay.threadSummary)
        assertEquals("article-17", newerDay.articles.single().id)
    }

    @Test
    fun `paging ignores requests beyond available bounds`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(date = LocalDate(2026, 4, 17), summary = "17 Apr summary", articleId = "article-17"),
                        day(date = LocalDate(2026, 4, 16), summary = "16 Apr summary", articleId = "article-16"),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 17),
        )

        advanceUntilIdle()

        assertFalse(viewModel.onNextDayClick())
        assertTrue(viewModel.onPreviousDayClick())
        assertFalse(viewModel.onPreviousDayClick())
    }

    @Test
    fun `success state exposes button availability for previous and next day`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(date = LocalDate(2026, 4, 17), summary = "17 Apr summary", articleId = "article-17"),
                        day(date = LocalDate(2026, 4, 16), summary = "16 Apr summary", articleId = "article-16"),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 17),
        )

        advanceUntilIdle()

        val initialSuccess = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState)
        assertTrue(initialSuccess.canGoToPreviousDay)
        assertFalse(initialSuccess.canGoToNextDay)

        assertTrue(viewModel.onPreviousDayClick())

        val olderDaySuccess = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState)
        assertFalse(olderDaySuccess.canGoToPreviousDay)
        assertTrue(olderDaySuccess.canGoToNextDay)
    }

    @Test
    fun `emits empty when history has no days`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(days = emptyList()),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 17),
        )

        advanceUntilIdle()

        assertEquals(ThreadHistoryContentState.Empty, viewModel.uiState.value.contentState)
    }

    @Test
    fun `emits error when loading thread history fails`() = runTest {
        val repository = TrackingRepository(
            historyResult = Result.failure(IllegalStateException("History failed")),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 17),
        )

        advanceUntilIdle()

        assertEquals(
            ThreadHistoryContentState.Error("History failed"),
            viewModel.uiState.value.contentState,
        )
    }

    @Test
    fun `read cache updates loaded history without refetch`() = runTest {
        val readArticlesCache = InMemoryReadArticlesCache()
        val repository = TrackingRepository(
            historyResult = Result.success(
                sampleHistory(
                    days = listOf(
                        day(
                            date = LocalDate(2026, 4, 17),
                            summary = "17 Apr summary",
                            articleId = "article-17",
                            isRead = false,
                        ),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDay = LocalDate(2026, 4, 17),
            readArticlesCache = readArticlesCache,
        )

        advanceUntilIdle()
        readArticlesCache.markRead("article-17")
        advanceUntilIdle()

        val success = assertIs<ThreadHistoryContentState.Success>(viewModel.uiState.value.contentState)
        assertEquals(true, success.visibleDay().articles.single().isRead)
        assertEquals("thread-1", repository.requestedThreadId)
    }
}

private fun createViewModel(
    repository: TrackingRepository,
    initialDay: LocalDate,
    readArticlesCache: InMemoryReadArticlesCache = InMemoryReadArticlesCache(),
): ThreadHistoryViewModel = ThreadHistoryViewModel(
    openThreadHistory = OpenThreadHistory(
        threadId = "thread-1",
        threadName = "Thread 1",
        initialDay = initialDay,
    ),
    getThreadHistoryUseCase = GetThreadHistoryUseCase(repository),
    readArticlesCache = readArticlesCache,
)

private class TrackingRepository(
    private val historyResult: Result<ThreadHistory>,
) : ThulurApiRepository {
    var requestedThreadId: String? = null

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

    override suspend fun getThreadHistory(threadId: String): ThreadHistory {
        requestedThreadId = threadId
        return historyResult.getOrThrow()
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

    override suspend fun sendGeneralChatMessage(message: String): String =
        error("Not used in this test")

    override suspend fun sendThreadChatMessage(threadId: String, message: String): String =
        error("Not used in this test")

    override suspend fun rateArticle(articleId: String, rating: Int) =
        error("Not used in this test")
}

private fun sampleHistory(
    days: List<ThreadHistoryDay>,
) = ThreadHistory(
    threadId = "thread-1",
    threadName = "Thread 1",
    days = days,
)

private fun day(
    date: LocalDate,
    summary: String,
    articleId: String,
    isRead: Boolean = false,
) = ThreadHistoryDay(
    day = date,
    threadSummary = summary,
    articles = listOf(
        Article(
            id = articleId,
            feedId = "feed-$articleId",
            title = "Title $articleId",
            url = "https://example.com/$articleId",
            published = "2026-04-17T08:00:00",
            displaySummary = "Display $articleId",
            isRead = isRead,
            isSuggestion = false,
            quality = ArticleQuality.Default,
        ),
    ),
)

private fun ThreadHistoryContentState.Success.visibleDay(): ThreadHistoryDay =
    history.days[visibleDayIndex]
