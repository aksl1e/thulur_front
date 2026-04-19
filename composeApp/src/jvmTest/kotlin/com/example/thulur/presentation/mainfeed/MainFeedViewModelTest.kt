package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import com.example.thulur.presentation.composables.TopicsViewMode
import com.example.thulur.presentation.composables.ThulurArticleItemVariant
import com.example.thulur.presentation.composables.ThulurThreadArticleData
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
class MainFeedViewModelTest {
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
            result = Result.success<List<MainFeedThread>>(emptyList()),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState(
                selectedDay = today,
                contentState = MainFeedContentState.Empty,
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `emits success when repository returns threads`() = runTest {
        val threads = listOf(sampleThread())
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(threads),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState(
                selectedDay = today,
                articleVisibilityByThreadId = visibleArticlesMap("thread-1" to true),
                contentState = MainFeedContentState.Success(threads),
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `emits error when repository throws`() = runTest {
        val repository = TrackingRepository(
            result = Result.failure<List<MainFeedThread>>(IllegalStateException("Boom")),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState(
                selectedDay = today,
                contentState = MainFeedContentState.Error("Boom"),
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `back click changes selected day and reloads feed`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

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
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

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
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()

        assertEquals(today, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `retry reloads the currently selected day`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        assertEquals(listOf<LocalDate?>(today, yesterday, yesterday), repository.requestedDays)
    }

    @Test
    fun `retry reloads the currently selected day after forward navigation`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(listOf<LocalDate?>(today, today.minus(1, DateTimeUnit.DAY), today, today), repository.requestedDays)
    }

    @Test
    fun `topics mode updates locally without reloading data`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

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
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

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
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread("thread-1"), sampleThread("thread-2"))),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

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
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread("thread-1"), sampleThread("thread-2"))),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

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
    fun `article click opens reader destination`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onArticleClick(sampleArticleData())

        assertEquals(
            OpenArticle(
                articleId = "article-1",
                title = "Article 1",
                url = "https://example.com/articles/1",
            ),
            viewModel.uiState.value.openArticle,
        )
    }

    @Test
    fun `show whole subject opens history destination with current selected day`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.onShowWholeSubjectClick(threadId = "thread-1", threadName = "Thread 1")

        assertEquals(
            OpenThreadHistory(
                threadId = "thread-1",
                threadName = "Thread 1",
                initialDay = today.minus(1, DateTimeUnit.DAY),
            ),
            viewModel.uiState.value.openThreadHistory,
        )
    }

    @Test
    fun `closing history clears open thread history without affecting content`() = runTest {
        val threads = listOf(sampleThread())
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(threads),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onShowWholeSubjectClick(threadId = "thread-1", threadName = "Thread 1")
        viewModel.onCloseThreadHistory()

        assertEquals(null, viewModel.uiState.value.openThreadHistory)
        assertEquals(MainFeedContentState.Success(threads), viewModel.uiState.value.contentState)
    }

    @Test
    fun `closing reader clears open article without affecting feed content`() = runTest {
        val threads = listOf(sampleThread())
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(threads),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onArticleClick(sampleArticleData())
        viewModel.onCloseArticleReader()

        assertEquals(null, viewModel.uiState.value.openArticle)
        assertEquals(MainFeedContentState.Success(threads), viewModel.uiState.value.contentState)
    }

    @Test
    fun `closing reader returns to whole subject when history is still open`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onShowWholeSubjectClick(threadId = "thread-1", threadName = "Thread 1")
        viewModel.onArticleClick(sampleArticleData())
        viewModel.onCloseArticleReader()

        assertEquals(null, viewModel.uiState.value.openArticle)
        assertEquals(
            OpenThreadHistory(
                threadId = "thread-1",
                threadName = "Thread 1",
                initialDay = today,
            ),
            viewModel.uiState.value.openThreadHistory,
        )
    }
}

private class TrackingRepository(
    private val result: Result<List<MainFeedThread>>,
) : ThulurApiRepository {
    val requestedDays = mutableListOf<LocalDate?>()

    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> {
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

    override suspend fun followFeed(feedId: String) =
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
}

private fun sampleThread(id: String = "thread-1") = MainFeedThread(
    id = id,
    name = "Thread 1",
    topicId = null,
    topicName = null,
    mainFeedScore = 0.8,
    firstSeen = null,
    summary = null,
    articles = emptyList(),
)

private fun visibleArticlesMap(vararg entries: Pair<String, Boolean>): Map<String, Boolean> = mapOf(*entries)

private fun sampleArticleData() = ThulurThreadArticleData(
    id = "article-1",
    url = "https://example.com/articles/1",
    variant = ThulurArticleItemVariant.Default,
    title = "Article 1",
    summary = "Summary",
    sourceLabel = "example.com",
    dateText = "17.04.2026",
    timeText = "12:00",
)
