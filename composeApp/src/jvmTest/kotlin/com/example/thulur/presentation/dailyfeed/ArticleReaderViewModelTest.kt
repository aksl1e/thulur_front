package com.example.thulur.presentation.dailyfeed

import com.example.thulur.data.session.InMemoryReadArticlesCache
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.RateArticleUseCase
import com.example.thulur.presentation.dailyfeed.article_reader.ArticleReaderViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
class ArticleReaderViewModelTest {
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
    fun `loads paragraphs and stays hidden until page and injection are complete`() = runTest {
        val repository = ReaderRepository(
            paragraphsResult = Result.success(
                listOf(
                    ArticleParagraph(idx = 0, text = "Novel", isNovel = true),
                ),
            ),
        )
        val viewModel = createViewModel(repository = repository)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.areParagraphsLoaded)
        assertFalse(viewModel.uiState.value.isReady)

        viewModel.onInitialPageLoaded()
        assertFalse(viewModel.uiState.value.isReady)

        viewModel.onInjectionSucceeded()
        assertTrue(viewModel.uiState.value.isReady)
    }

    @Test
    fun `progress is clamped and only moves upward`() = runTest {
        val repository = ReaderRepository(
            paragraphsResult = Result.success(emptyList()),
        )
        val viewModel = createViewModel(repository = repository)

        advanceUntilIdle()
        viewModel.onProgressChanged(0.35f)
        viewModel.onProgressChanged(0.10f)
        viewModel.onProgressChanged(1.25f)

        assertEquals(1f, viewModel.uiState.value.readProgress)
    }

    @Test
    fun `paragraph failure turns reader into error state`() = runTest {
        val repository = ReaderRepository(
            paragraphsResult = Result.failure(IllegalStateException("Paragraphs failed")),
        )
        val viewModel = createViewModel(repository = repository)

        advanceUntilIdle()

        assertEquals("Paragraphs failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isReady)
    }

    @Test
    fun `successful submit marks article as read in cache and ui state`() = runTest {
        val readArticlesCache = InMemoryReadArticlesCache()
        val repository = ReaderRepository(
            paragraphsResult = Result.success(emptyList()),
            rateResult = Result.success(Unit),
        )
        val viewModel = createViewModel(
            repository = repository,
            readArticlesCache = readArticlesCache,
        )

        advanceUntilIdle()
        viewModel.onRateArticle(3)
        viewModel.submitRate()
        advanceUntilIdle()

        assertEquals("article-1", repository.ratedArticleId)
        assertEquals(3, repository.ratedValue)
        assertTrue(readArticlesCache.isRead("article-1"))
        assertTrue(viewModel.uiState.value.isArticleRead)
    }

    @Test
    fun `failed submit does not update read cache`() = runTest {
        val readArticlesCache = InMemoryReadArticlesCache()
        val repository = ReaderRepository(
            paragraphsResult = Result.success(emptyList()),
            rateResult = Result.failure(IllegalStateException("Rate failed")),
        )
        val viewModel = createViewModel(
            repository = repository,
            readArticlesCache = readArticlesCache,
        )

        advanceUntilIdle()
        viewModel.onRateArticle(4)
        viewModel.submitRate()
        advanceUntilIdle()

        assertEquals("article-1", repository.ratedArticleId)
        assertEquals(4, repository.ratedValue)
        assertFalse(readArticlesCache.isRead("article-1"))
        assertFalse(viewModel.uiState.value.isArticleRead)
    }

    @Test
    fun `already read article skips submit`() = runTest {
        val repository = ReaderRepository(
            paragraphsResult = Result.success(emptyList()),
            rateResult = Result.success(Unit),
        )
        val viewModel = createViewModel(
            repository = repository,
            isRead = true,
        )

        advanceUntilIdle()
        viewModel.onRateArticle(5)
        viewModel.submitRate()
        advanceUntilIdle()

        assertEquals(0, repository.rateCalls)
    }
}

private fun createViewModel(
    repository: ReaderRepository,
    readArticlesCache: InMemoryReadArticlesCache = InMemoryReadArticlesCache(),
    articleId: String = "article-1",
    title: String = "Article 1",
    url: String = "https://example.com/articles/1",
    isRead: Boolean = false,
): ArticleReaderViewModel = ArticleReaderViewModel(
    articleId = articleId,
    title = title,
    url = url,
    isRead = isRead,
    getArticleParagraphsUseCase = GetArticleParagraphsUseCase(repository),
    rateArticleUseCase = RateArticleUseCase(repository),
    readArticlesCache = readArticlesCache,
)

private class ReaderRepository(
    private val paragraphsResult: Result<List<ArticleParagraph>>,
    private val rateResult: Result<Unit> = Result.success(Unit),
) : ThulurApiRepository {
    var rateCalls = 0
        private set
    var ratedArticleId: String? = null
        private set
    var ratedValue: Int? = null
        private set

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed =
        DailyFeed(isDefault = true, threads = emptyList())

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        paragraphsResult.getOrThrow()

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

    override suspend fun rateArticle(articleId: String, rating: Int) {
        rateCalls += 1
        ratedArticleId = articleId
        ratedValue = rating
        rateResult.getOrThrow()
    }
}
