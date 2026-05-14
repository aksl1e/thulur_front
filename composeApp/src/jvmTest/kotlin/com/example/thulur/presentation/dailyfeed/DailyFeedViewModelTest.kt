package com.example.thulur.presentation.dailyfeed

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
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.GetDailyFeedUseCase
import com.example.thulur.presentation.composables.TopicsViewMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DailyFeedViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits empty when repository returns no threads`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed(isDefault = true, threads = emptyList())),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            DailyFeedUiState(
                selectedDay = today,
                isDefault = true,
                contentState = DailyFeedContentState.Empty,
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `emits success when repository returns threads`() = runTest {
        val threads = listOf(sampleThread())
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed(isDefault = false, threads = threads)),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            DailyFeedUiState(
                selectedDay = today,
                isDefault = false,
                articleVisibilityByThreadId = visibleArticlesMap("thread-1" to true),
                contentState = DailyFeedContentState.Success(threads),
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `emits error when repository throws`() = runTest {
        val repository = TrackingRepository(
            result = Result.failure<DailyFeed>(IllegalStateException("Boom")),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            DailyFeedUiState(
                selectedDay = today,
                contentState = DailyFeedContentState.Error("Boom"),
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `back click changes selected day and reloads feed`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        assertEquals(yesterday, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today, yesterday), repository.requestedDays)
    }

    @Test
    fun `forward click changes selected day toward today and reloads feed`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()

        assertEquals(today, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today, today.minus(1, DateTimeUnit.DAY), today), repository.requestedDays)
    }

    @Test
    fun `forward click on today does nothing`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()

        assertEquals(today, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `retry reloads the currently selected day`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        assertEquals(listOf<LocalDate?>(today, yesterday, yesterday), repository.requestedDays)
    }

    @Test
    fun `topics mode updates locally without reloading data`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onTopicsViewModeChange(TopicsViewMode.TopicsOnly)

        assertEquals(TopicsViewMode.TopicsOnly, viewModel.uiState.value.topicsViewMode)
        assertEquals(
            visibleArticlesMap("thread-1" to false),
            viewModel.uiState.value.articleVisibilityByThreadId,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `thread visibility toggle overrides current per thread state`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onTopicsViewModeChange(TopicsViewMode.TopicsOnly)
        viewModel.onThreadArticlesVisibilityToggle("thread-1")

        assertEquals(
            visibleArticlesMap("thread-1" to true),
            viewModel.uiState.value.articleVisibilityByThreadId,
        )
    }

    @Test
    fun `global topics switch overwrites mixed per thread visibility`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(
                sampleDailyFeed(threads = listOf(sampleThread("thread-1"), sampleThread("thread-2"))),
            ),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onThreadArticlesVisibilityToggle("thread-1")
        viewModel.onTopicsViewModeChange(TopicsViewMode.TopicsOnly)
        assertEquals(
            visibleArticlesMap("thread-1" to false, "thread-2" to false),
            viewModel.uiState.value.articleVisibilityByThreadId,
        )

        viewModel.onTopicsViewModeChange(TopicsViewMode.TopicsAndArticles)
        assertEquals(
            visibleArticlesMap("thread-1" to true, "thread-2" to true),
            viewModel.uiState.value.articleVisibilityByThreadId,
        )
    }

    @Test
    fun `loading a new day resets article visibility according to current topics mode`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(
                sampleDailyFeed(threads = listOf(sampleThread("thread-1"), sampleThread("thread-2"))),
            ),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onThreadArticlesVisibilityToggle("thread-1")
        viewModel.onTopicsViewModeChange(TopicsViewMode.TopicsOnly)
        viewModel.onBackClick()
        advanceUntilIdle()

        assertEquals(
            visibleArticlesMap("thread-1" to false, "thread-2" to false),
            viewModel.uiState.value.articleVisibilityByThreadId,
        )
    }

    @Test
    fun `feed scroll state updates locally`() = runTest {
        val repository = TrackingRepository(
            result = Result.success(sampleDailyFeed()),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.onFeedScrollStateChange(index = 4, offset = 128)

        assertEquals(4, viewModel.uiState.value.feedScrollIndex)
        assertEquals(128, viewModel.uiState.value.feedScrollOffset)
    }

    @Test
    fun `read cache updates loaded feed without refetch`() = runTest {
        val readArticlesCache = InMemoryReadArticlesCache()
        val repository = TrackingRepository(
            result = Result.success(
                sampleDailyFeed(
                    threads = listOf(
                        sampleThread(
                            articles = listOf(sampleDomainArticle(isRead = false)),
                        ),
                    ),
                ),
            ),
        )
        val viewModel = createViewModel(repository, readArticlesCache)

        advanceUntilIdle()
        readArticlesCache.markRead("article-1")
        advanceUntilIdle()

        val threads = (viewModel.uiState.value.contentState as DailyFeedContentState.Success).threads
        assertEquals(true, threads.single().articles.single().isRead)
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }
}

private fun createViewModel(
    repository: TrackingRepository,
    readArticlesCache: InMemoryReadArticlesCache = InMemoryReadArticlesCache(),
): DailyFeedViewModel = DailyFeedViewModel(
    getDailyFeedUseCase = GetDailyFeedUseCase(repository),
    readArticlesCache = readArticlesCache,
)

private class TrackingRepository(
    private val result: Result<DailyFeed>,
) : ThulurApiRepository {
    val requestedDays = mutableListOf<LocalDate?>()

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed {
        requestedDays += day
        return result.getOrThrow()
    }

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

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

    override suspend fun getThreadHistory(threadId: String): ThreadHistory =
        error("Not used in this test")

    override suspend fun sendGeneralChatMessage(message: String): String =
        error("Not used in this test")

    override suspend fun sendThreadChatMessage(threadId: String, message: String): String =
        error("Not used in this test")

    override suspend fun rateArticle(articleId: String, rating: Int) =
        error("Not used in this test")
}

private fun sampleThread(
    id: String = "thread-1",
    articles: List<Article> = emptyList(),
) = DailyFeedThread(
    id = id,
    name = "Thread 1",
    topicId = null,
    topicName = null,
    dailyFeedScore = 0.8,
    firstSeen = null,
    summary = null,
    articles = articles,
)

private fun sampleDailyFeed(
    isDefault: Boolean = true,
    threads: List<DailyFeedThread> = listOf(sampleThread()),
) = DailyFeed(
    isDefault = isDefault,
    threads = threads,
)

private fun visibleArticlesMap(vararg entries: Pair<String, Boolean>): Map<String, Boolean> = mapOf(*entries)

private fun sampleDomainArticle(
    id: String = "article-1",
    isRead: Boolean = false,
) = Article(
    id = id,
    feedId = "feed-1",
    title = "Article 1",
    url = "https://example.com/articles/1",
    imageUrl = "https://example.com/articles/1.jpg",
    published = "2026-04-17T12:00:00",
    displaySummary = "Summary",
    isRead = isRead,
    isSuggestion = false,
    quality = ArticleQuality.Default,
)
