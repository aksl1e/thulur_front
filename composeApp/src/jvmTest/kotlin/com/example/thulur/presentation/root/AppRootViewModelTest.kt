package com.example.thulur.presentation.root

import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.data.session.InMemoryReadArticlesCache
import com.example.thulur.data.session.InMemorySecureTokenStore
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.theme.ThemeStore
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
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
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AppRootViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts in loading state before reading persisted token`() = runTest {
        val viewModel = createViewModel(createSessionProvider(InMemorySecureTokenStore()))

        assertEquals(AppRootUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `moves to unauthenticated when no persisted token exists`() = runTest {
        val viewModel = createViewModel(createSessionProvider(InMemorySecureTokenStore()))

        advanceUntilIdle()

        assertEquals(AppRootUiState.Unready(), viewModel.uiState.value)
    }

    @Test
    fun `moves to authenticated when persisted token exists`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)

        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 1), viewModel.uiState.value)
    }

    @Test
    fun `moves back to unauthenticated when token is cleared`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.clearToken()
        advanceUntilIdle()

        assertEquals(AppRootUiState.Unready(), viewModel.uiState.value)
    }

    @Test
    fun `increments session instance id when user authenticates again after logout`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 1), viewModel.uiState.value)

        sessionProvider.clearToken()
        advanceUntilIdle()
        assertEquals(AppRootUiState.Unready(), viewModel.uiState.value)

        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 2), viewModel.uiState.value)
    }

    @Test
    fun `does not increment session instance id when token changes inside active session`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()

        viewModel.openSettings()
        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 1,
                destination = AppRootAuthenticatedDestination.Settings,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `reads session instance id from current session provider`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.updateToken("token-1")
        advanceUntilIdle()
        assertEquals(AppRootUiState.Ready(sessionInstanceId = 1), viewModel.uiState.value)

        sessionProvider.clearToken()
        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 2), viewModel.uiState.value)
    }

    @Test
    fun `open settings switches authenticated destination`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()

        viewModel.openSettings()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 1,
                destination = AppRootAuthenticatedDestination.Settings,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `back to main feed returns authenticated destination to main feed`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()

        viewModel.openSettings()
        viewModel.backToDailyFeed()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 1,
                destination = AppRootAuthenticatedDestination.DailyFeed,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `new authenticated session resets destination to main feed`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = createViewModel(sessionProvider)
        advanceUntilIdle()
        viewModel.openSettings()

        sessionProvider.clearToken()
        advanceUntilIdle()
        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 2,
                destination = AppRootAuthenticatedDestination.DailyFeed,
            ),
            viewModel.uiState.value,
        )
    }

    private fun createViewModel(sessionProvider: CurrentSessionProviderImpl): AppRootViewModel =
        AppRootViewModel(sessionProvider, GetUserSettingsUseCase(StubSettingsRepository), InMemoryThemeStore())
}

private fun createSessionProvider(tokenStore: InMemorySecureTokenStore): CurrentSessionProviderImpl =
    CurrentSessionProviderImpl(
        tokenStore = tokenStore,
        readArticlesCache = InMemoryReadArticlesCache(),
    )

private class InMemoryThemeStore : ThemeStore {
    private var darkMode: Boolean? = null
    override suspend fun readDarkMode(): Boolean? = darkMode
    override suspend fun writeDarkMode(darkMode: Boolean) { this.darkMode = darkMode }
}

private object StubSettingsRepository : ThulurApiRepository {
    override suspend fun getUserSettings(): UserSettings = UserSettings(
        userId = "",
        darkMode = false,
        suggestionsOutside = false,
        minQualityScore = 0.0,
        language = "",
        notificationsEnabled = false,
        notificationsTime = "",
        timezone = "",
        updatedAt = "",
    )

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed = error("not used")
    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> = error("not used")
    override suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings = error("not used")
    override suspend fun getFollowedFeeds(): List<Feed> = error("not used")
    override suspend fun getAllFeeds(): List<Feed> = error("not used")
    override suspend fun followFeed(identifier: String): Unit = error("not used")
    override suspend fun unfollowFeed(feedId: String): Unit = error("not used")
    override suspend fun getCurrentUser(): CurrentUser = error("not used")
    override suspend fun getAuthSessions(): List<AuthSession> = error("not used")
    override suspend fun terminateAuthSession(sessionId: String): Unit = error("not used")
    override suspend fun getThreadHistory(threadId: String): ThreadHistory = error("not used")
    override suspend fun rateArticle(articleId: String, rating: Int): Unit = error("not used")
}
