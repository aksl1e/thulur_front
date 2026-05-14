package com.example.thulur.presentation.root

import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.data.session.InMemoryReadArticlesCache
import com.example.thulur.data.session.InMemorySecureTokenStore
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.theme.ThemeStore
import com.example.thulur.domain.usecase.GetCurrentUserUseCase
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.presentation.theme.ThemeMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
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
        val screenModel = createScreenModel(createSessionProvider(InMemorySecureTokenStore()))

        assertEquals(AppRootUiState.Loading, screenModel.uiState.value)
    }

    @Test
    fun `moves to unauthenticated when no persisted token exists`() = runTest {
        val screenModel = createScreenModel(createSessionProvider(InMemorySecureTokenStore()))

        advanceUntilIdle()

        assertEquals(AppRootUiState.Unready(), screenModel.uiState.value)
    }

    @Test
    fun `moves to authenticated when persisted token exists`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val screenModel = createScreenModel(sessionProvider)

        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 1), screenModel.uiState.value)
    }

    @Test
    fun `moves back to unauthenticated when token is cleared`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val screenModel = createScreenModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.clearToken()
        advanceUntilIdle()

        assertEquals(AppRootUiState.Unready(), screenModel.uiState.value)
    }

    @Test
    fun `increments session instance id when user authenticates again after logout`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val screenModel = createScreenModel(sessionProvider)
        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 1), screenModel.uiState.value)

        sessionProvider.clearToken()
        advanceUntilIdle()
        assertEquals(AppRootUiState.Unready(), screenModel.uiState.value)

        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 2), screenModel.uiState.value)
    }

    @Test
    fun `does not increment session instance id when token changes inside active session`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val screenModel = createScreenModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(
            AppRootUiState.Ready(sessionInstanceId = 1),
            screenModel.uiState.value,
        )
    }

    @Test
    fun `reads session instance id from current session provider`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val screenModel = createScreenModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.updateToken("token-1")
        advanceUntilIdle()
        assertEquals(AppRootUiState.Ready(sessionInstanceId = 1), screenModel.uiState.value)

        sessionProvider.clearToken()
        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Ready(sessionInstanceId = 2), screenModel.uiState.value)
    }

    @Test
    fun `update theme switches current ready theme and persists it`() = runTest {
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val themeStore = InMemoryThemeStore()
        val screenModel = createScreenModel(sessionProvider, themeStore = themeStore)
        advanceUntilIdle()

        screenModel.updateTheme(ThemeMode.Dark)
        advanceUntilIdle()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 1,
                themeMode = ThemeMode.Dark,
            ),
            screenModel.uiState.value,
        )
        assertEquals(true, themeStore.readDarkMode())
    }

    @Test
    fun `subscription tier starts unknown and updates after current user loads`() = runTest {
        val currentUserDeferred = CompletableDeferred<CurrentUser>()
        val repository = TrackingRootRepository(currentUserDeferred = currentUserDeferred)
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val screenModel = createScreenModel(sessionProvider, repository)

        advanceUntilIdle()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 1,
                subscriptionTier = AppSubscriptionTier.Unknown,
            ),
            screenModel.uiState.value,
        )

        currentUserDeferred.complete(sampleCurrentUser(subscriptionTier = "pro"))
        advanceUntilIdle()

        assertEquals(
            AppRootUiState.Ready(
                sessionInstanceId = 1,
                subscriptionTier = AppSubscriptionTier.Pro,
            ),
            screenModel.uiState.value,
        )
    }

    @Test
    fun `subscription retries with configured backoff and updates after success`() = runTest {
        val repository = TrackingRootRepository(
            currentUserResponses = ArrayDeque(
                listOf(
                    Result.failure(IllegalStateException("offline")),
                    Result.failure(IllegalStateException("offline")),
                    Result.success(sampleCurrentUser(subscriptionTier = "corporate")),
                ),
            ),
        )
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore(initialToken = "token-1"))
        val screenModel = createScreenModel(sessionProvider, repository)

        runCurrent()

        assertEquals(1, repository.getCurrentUserCallCount)
        assertEquals(AppSubscriptionTier.Unknown, (screenModel.uiState.value as AppRootUiState.Ready).subscriptionTier)

        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(1, repository.getCurrentUserCallCount)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, repository.getCurrentUserCallCount)
        assertEquals(AppSubscriptionTier.Unknown, (screenModel.uiState.value as AppRootUiState.Ready).subscriptionTier)

        advanceTimeBy(14_999)
        runCurrent()
        assertEquals(2, repository.getCurrentUserCallCount)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(3, repository.getCurrentUserCallCount)
        assertEquals(
            AppSubscriptionTier.Corporate,
            (screenModel.uiState.value as AppRootUiState.Ready).subscriptionTier,
        )

        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(3, repository.getCurrentUserCallCount)
    }

    private fun createScreenModel(
        sessionProvider: CurrentSessionProviderImpl,
        repository: TrackingRootRepository = TrackingRootRepository(),
        themeStore: InMemoryThemeStore = InMemoryThemeStore(),
    ): AppRootScreenModel = AppRootScreenModel(
        sessionProvider,
        GetUserSettingsUseCase(repository),
        GetCurrentUserUseCase(repository),
        themeStore,
    )
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

private class TrackingRootRepository(
    private val currentUser: CurrentUser = sampleCurrentUser(),
    private val currentUserDeferred: CompletableDeferred<CurrentUser>? = null,
    private val currentUserResponses: ArrayDeque<Result<CurrentUser>> = ArrayDeque(),
) : ThulurApiRepository {
    var getCurrentUserCallCount: Int = 0

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
    override suspend fun getCurrentUser(): CurrentUser {
        getCurrentUserCallCount += 1
        currentUserDeferred?.let { return it.await() }
        if (currentUserResponses.isNotEmpty()) {
            return currentUserResponses.removeFirst().getOrThrow()
        }
        return currentUser
    }
    override suspend fun getAuthSessions(): List<AuthSession> = error("not used")
    override suspend fun terminateAuthSession(sessionId: String): Unit = error("not used")
    override suspend fun getThreadHistory(threadId: String): ThreadHistory = error("not used")
    override suspend fun sendGeneralChatMessage(message: String): String = error("not used")
    override suspend fun sendThreadChatMessage(threadId: String, message: String): String = error("not used")
    override suspend fun rateArticle(articleId: String, rating: Int): Unit = error("not used")
}

private fun sampleCurrentUser(subscriptionTier: String = "unknown"): CurrentUser = CurrentUser(
    id = "user-1",
    email = "user@example.com",
    subscriptionTier = subscriptionTier,
    subscriptionExpiresAt = null,
    createdAt = "2026-01-01T00:00:00Z",
)
