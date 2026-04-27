package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.RateArticleUseCase
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
        val viewModel = ArticleReaderViewModel(
            openArticle = OpenArticle(
                articleId = "article-1",
                title = "Article 1",
                url = "https://example.com/articles/1",
            ),
            getArticleParagraphsUseCase = GetArticleParagraphsUseCase(
                ParagraphRepository(
                    paragraphsResult = Result.success(
                        listOf(
                            ArticleParagraph(idx = 0, text = "Novel", isNovel = true),
                        ),
                    ),
                ),
            ),
            rateArticleUseCase = RateArticleUseCase(
                ParagraphRepository(
                    paragraphsResult = Result.success(emptyList()),
                ),
            ),
        )

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
        val viewModel = ArticleReaderViewModel(
            openArticle = OpenArticle(
                articleId = "article-1",
                title = "Article 1",
                url = "https://example.com/articles/1",
            ),
            getArticleParagraphsUseCase = GetArticleParagraphsUseCase(
                ParagraphRepository(
                    paragraphsResult = Result.success(emptyList()),
                ),
            ),
            rateArticleUseCase = RateArticleUseCase(
                ParagraphRepository(
                    paragraphsResult = Result.success(emptyList()),
                ),
            ),
        )

        advanceUntilIdle()
        viewModel.onProgressChanged(0.35f)
        viewModel.onProgressChanged(0.10f)
        viewModel.onProgressChanged(1.25f)

        assertEquals(1f, viewModel.uiState.value.readProgress)
    }

    @Test
    fun `paragraph failure turns reader into error state`() = runTest {
        val viewModel = ArticleReaderViewModel(
            openArticle = OpenArticle(
                articleId = "article-1",
                title = "Article 1",
                url = "https://example.com/articles/1",
            ),
            getArticleParagraphsUseCase = GetArticleParagraphsUseCase(
                ParagraphRepository(
                    paragraphsResult = Result.failure(IllegalStateException("Paragraphs failed")),
                ),
            ),
            rateArticleUseCase = RateArticleUseCase(
                ParagraphRepository(
                    paragraphsResult = Result.success(emptyList()),
                ),
            ),
        )

        advanceUntilIdle()

        assertEquals("Paragraphs failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isReady)
    }
}

private class ParagraphRepository(
    private val paragraphsResult: Result<List<ArticleParagraph>>,
) : ThulurApiRepository {
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

    override suspend fun rateArticle(articleId: String, rating: Int) =
        error("Not used in this test")
}
