package com.example.thulur.presentation.settings

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.FollowFeedUseCase
import com.example.thulur.domain.usecase.GetAllFeedsUseCase
import com.example.thulur.domain.usecase.GetAuthSessionsUseCase
import com.example.thulur.domain.usecase.GetCurrentUserUseCase
import com.example.thulur.domain.usecase.GetFollowedFeedsUseCase
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.domain.usecase.PatchUserSettingsUseCase
import com.example.thulur.domain.usecase.TerminateAuthSessionUseCase
import com.example.thulur.domain.usecase.UnfollowFeedUseCase
import com.example.thulur.presentation.theme.ThemeMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
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
    fun `initial load success maps repository settings account state and feeds state`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(SettingsContentState.Ready, viewModel.uiState.value.contentState)
        assertEquals(
            SettingsAppValues(
                feedSchedule = expectedDisplayFeedSchedule("8:45:00"),
                theme = ThemeMode.Dark,
                language = "Polish",
                notificationsEnabled = true,
                timezone = "Europe/Warsaw",
                suggestionsOutside = false,
            ),
            viewModel.uiState.value.appState.values,
        )
        assertEquals("hello@example.com", viewModel.uiState.value.accountState.currentEmail)
        assertEquals(expectedSettingsSessions(), viewModel.uiState.value.accountState.sessions)
        assertEquals(false, viewModel.uiState.value.accountState.isLoading)
        assertEquals(sampleFollowedFeeds(), viewModel.uiState.value.feedsState.followedFeeds)
        assertEquals(sampleAllFeeds(), viewModel.uiState.value.feedsState.catalogFeeds)
        assertEquals(expectedAvailableFeeds(), viewModel.uiState.value.feedsState.visibleAvailableFeeds)
        assertEquals(false, viewModel.uiState.value.feedsState.isLoading)
        assertEquals(false, viewModel.uiState.value.feedsState.isError)
        assertEquals(1, repository.getUserSettingsCallCount)
        assertEquals(1, repository.getCurrentUserCallCount)
        assertEquals(1, repository.getAuthSessionsCallCount)
        assertEquals(1, repository.getFollowedFeedsCallCount)
        assertEquals(1, repository.getAllFeedsCallCount)
    }

    @Test
    fun `initial load failure exposes error and retry recovers`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextGetSettingsFailure = IllegalStateException("Offline"),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        assertEquals(
            SettingsContentState.Error("Offline"),
            viewModel.uiState.value.contentState,
        )

        viewModel.retryLoad()
        advanceUntilIdle()

        assertEquals(SettingsContentState.Ready, viewModel.uiState.value.contentState)
        assertEquals(2, repository.getUserSettingsCallCount)
        assertEquals(1, repository.getFollowedFeedsCallCount)
        assertEquals(1, repository.getAllFeedsCallCount)
    }

    @Test
    fun `account load failure does not block app settings and keeps session data when available`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextGetCurrentUserFailure = IllegalStateException("Account offline"),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(SettingsContentState.Ready, viewModel.uiState.value.contentState)
        assertEquals(null, viewModel.uiState.value.accountState.currentEmail)
        assertEquals(expectedSettingsSessions(), viewModel.uiState.value.accountState.sessions)
        assertEquals(sampleFollowedFeeds(), viewModel.uiState.value.feedsState.followedFeeds)
    }

    @Test
    fun `subscription stays disabled while feeds can be selected`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onSectionSelected(SettingsSection.Subscription)
        assertEquals(SettingsSection.AccountAndApp, viewModel.uiState.value.selectedSection)

        viewModel.onSectionSelected(SettingsSection.Feeds)

        assertEquals(SettingsSection.Feeds, viewModel.uiState.value.selectedSection)
    }

    @Test
    fun `theme selection patches only dark mode`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.Light)
        advanceUntilIdle()

        assertEquals(
            PatchUserSettings(darkMode = false),
            repository.updates.single(),
        )
    }

    @Test
    fun `language selection patches only language`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onLanguageSelected("English")
        advanceUntilIdle()

        assertEquals(
            PatchUserSettings(language = "en"),
            repository.updates.single(),
        )
    }

    @Test
    fun `timezone selection patches only timezone`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onTimezoneSelected("UTC")
        advanceUntilIdle()

        assertEquals(
            PatchUserSettings(timezone = "UTC"),
            repository.updates.single(),
        )
    }

    @Test
    fun `notification toggle patches only notifications enabled`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onNotificationsEnabledChanged(false)
        advanceUntilIdle()

        assertEquals(
            PatchUserSettings(notificationsEnabled = false),
            repository.updates.single(),
        )
    }

    @Test
    fun `suggestions outside toggle patches only suggestions outside`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onSuggestionsOutsideChanged(true)
        advanceUntilIdle()

        assertEquals(
            PatchUserSettings(suggestionsOutside = true),
            repository.updates.single(),
        )
    }

    @Test
    fun `feed schedule change parses and patches canonical time`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFeedScheduleChanged(FeedScheduleValue(hour = 9, minute = 5))
        advanceUntilIdle()

        assertEquals(
            PatchUserSettings(notificationsTime = expectedBackendTime(FeedScheduleValue(hour = 9, minute = 5))),
            repository.updates.single(),
        )
        assertEquals(
            FeedScheduleValue(hour = 9, minute = 5),
            viewModel.uiState.value.appState.values.feedSchedule,
        )
    }

    @Test
    fun `update failure rolls back optimistic value and exposes inline error`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextUpdateFailure = IllegalStateException("Nope"),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onNotificationsEnabledChanged(false)
        assertEquals(false, viewModel.uiState.value.appState.values.notificationsEnabled)

        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.appState.values.notificationsEnabled)
        assertTrue(viewModel.uiState.value.appState.pendingFields.isEmpty())
    }

    @Test
    fun `terminate session refetches account sessions on success`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onTerminateSessionClick("session-1")
        assertEquals(setOf("session-1"), viewModel.uiState.value.accountState.terminatingSessionIds)

        advanceUntilIdle()

        assertEquals(listOf("session-1"), repository.terminatedSessionIds)
        assertEquals(2, repository.getAuthSessionsCallCount)
        assertEquals(
            listOf(expectedSettingsSessions().last()),
            viewModel.uiState.value.accountState.sessions,
        )
        assertTrue(viewModel.uiState.value.accountState.terminatingSessionIds.isEmpty())
    }

    @Test
    fun `terminate failure preserves session rows and exposes account error`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextTerminateFailure = IllegalStateException("Terminate failed"),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onTerminateSessionClick("session-1")
        advanceUntilIdle()

        assertEquals(expectedSettingsSessions(), viewModel.uiState.value.accountState.sessions)
        assertTrue(viewModel.uiState.value.accountState.terminatingSessionIds.isEmpty())
    }

    @Test
    fun `feed search filters available feeds by url and chip values including language`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFeedSearchQueryChanged("techcrunch")
        assertEquals(
            listOf("feed-available-1"),
            viewModel.uiState.value.feedsState.visibleAvailableFeeds.map(Feed::id),
        )

        viewModel.onFeedSearchQueryChanged("mobile")
        assertEquals(
            listOf("feed-available-2"),
            viewModel.uiState.value.feedsState.visibleAvailableFeeds.map(Feed::id),
        )

        viewModel.onFeedSearchQueryChanged("Polish")
        assertEquals(
            listOf("feed-available-2"),
            viewModel.uiState.value.feedsState.visibleAvailableFeeds.map(Feed::id),
        )

        viewModel.onFeedSearchQueryChanged("")
        assertEquals(
            expectedAvailableFeeds().map(Feed::id),
            viewModel.uiState.value.feedsState.visibleAvailableFeeds.map(Feed::id),
        )
    }

    @Test
    fun `follow success moves item from available to followed`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFollowFeedClick("feed-available-1")
        assertEquals(setOf("feed-available-1"), viewModel.uiState.value.feedsState.pendingFollowIds)

        advanceUntilIdle()

        assertEquals(listOf("feed-available-1"), repository.followedFeedIds)
        assertTrue(viewModel.uiState.value.feedsState.pendingFollowIds.isEmpty())
        assertEquals(
            listOf("feed-followed-1", "feed-followed-2", "feed-available-1"),
            viewModel.uiState.value.feedsState.followedFeeds.map(Feed::id),
        )
        assertEquals(
            listOf("feed-available-2"),
            viewModel.uiState.value.feedsState.visibleAvailableFeeds.map(Feed::id),
        )
    }

    @Test
    fun `unfollow success moves item from followed back into available in catalog order`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onUnfollowFeedClick("feed-followed-1")
        assertEquals(setOf("feed-followed-1"), viewModel.uiState.value.feedsState.pendingUnfollowIds)

        advanceUntilIdle()

        assertEquals(listOf("feed-followed-1"), repository.unfollowedFeedIds)
        assertTrue(viewModel.uiState.value.feedsState.pendingUnfollowIds.isEmpty())
        assertEquals(
            listOf("feed-followed-2"),
            viewModel.uiState.value.feedsState.followedFeeds.map(Feed::id),
        )
        assertEquals(
            listOf("feed-followed-1", "feed-available-1", "feed-available-2"),
            viewModel.uiState.value.feedsState.visibleAvailableFeeds.map(Feed::id),
        )
    }

    @Test
    fun `feeds load failure exposes feeds error without blocking app settings and account state`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextGetAllFeedsFailure = IllegalStateException("Feeds offline"),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(SettingsContentState.Ready, viewModel.uiState.value.contentState)
        assertEquals("hello@example.com", viewModel.uiState.value.accountState.currentEmail)
        assertEquals(true, viewModel.uiState.value.feedsState.isError)
        assertEquals(sampleFollowedFeeds(), viewModel.uiState.value.feedsState.followedFeeds)
        assertTrue(viewModel.uiState.value.feedsState.catalogFeeds.isEmpty())
    }

    @Test
    fun `follow failure keeps lists intact and exposes feeds error`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextFollowFailure = IllegalStateException("Follow failed"),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onFollowFeedClick("feed-available-1")
        advanceUntilIdle()

        assertEquals(sampleFollowedFeeds(), viewModel.uiState.value.feedsState.followedFeeds)
        assertEquals(expectedAvailableFeeds(), viewModel.uiState.value.feedsState.visibleAvailableFeeds)
        assertTrue(viewModel.uiState.value.feedsState.pendingFollowIds.isEmpty())
    }

    @Test
    fun `unfollow failure keeps lists intact and exposes feeds error`() = runTest {
        val repository = FakeSettingsRepository(
            currentSettings = sampleUserSettings(),
            currentUser = sampleCurrentUser(),
            currentAuthSessions = sampleAuthSessions(),
            currentFollowedFeeds = sampleFollowedFeeds(),
            currentAllFeeds = sampleAllFeeds(),
            nextUnfollowFailure = IllegalStateException("Unfollow failed"),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onUnfollowFeedClick("feed-followed-1")
        advanceUntilIdle()

        assertEquals(sampleFollowedFeeds(), viewModel.uiState.value.feedsState.followedFeeds)
        assertEquals(expectedAvailableFeeds(), viewModel.uiState.value.feedsState.visibleAvailableFeeds)
        assertTrue(viewModel.uiState.value.feedsState.pendingUnfollowIds.isEmpty())
    }

    private fun createViewModel(repository: FakeSettingsRepository): SettingsViewModel = SettingsViewModel(
        getUserSettingsUseCase = GetUserSettingsUseCase(repository),
        patchUserSettingsUseCase = PatchUserSettingsUseCase(repository),
        getCurrentUserUseCase = GetCurrentUserUseCase(repository),
        getAuthSessionsUseCase = GetAuthSessionsUseCase(repository),
        terminateAuthSessionUseCase = TerminateAuthSessionUseCase(repository),
        getFollowedFeedsUseCase = GetFollowedFeedsUseCase(repository),
        getAllFeedsUseCase = GetAllFeedsUseCase(repository),
        followFeedUseCase = FollowFeedUseCase(repository),
        unfollowFeedUseCase = UnfollowFeedUseCase(repository),
    )
}

private class FakeSettingsRepository(
    var currentSettings: UserSettings,
    var currentUser: CurrentUser,
    var currentAuthSessions: List<AuthSession>,
    var currentFollowedFeeds: List<Feed>,
    var currentAllFeeds: List<Feed>,
    var nextGetSettingsFailure: Throwable? = null,
    var nextGetCurrentUserFailure: Throwable? = null,
    var nextGetAuthSessionsFailure: Throwable? = null,
    var nextGetFollowedFeedsFailure: Throwable? = null,
    var nextGetAllFeedsFailure: Throwable? = null,
    var nextUpdateFailure: Throwable? = null,
    var nextTerminateFailure: Throwable? = null,
    var nextFollowFailure: Throwable? = null,
    var nextUnfollowFailure: Throwable? = null,
) : ThulurApiRepository {
    var getUserSettingsCallCount: Int = 0
        private set
    var getCurrentUserCallCount: Int = 0
        private set
    var getAuthSessionsCallCount: Int = 0
        private set
    var getFollowedFeedsCallCount: Int = 0
        private set
    var getAllFeedsCallCount: Int = 0
        private set

    val updates = mutableListOf<PatchUserSettings>()
    val terminatedSessionIds = mutableListOf<String>()
    val followedFeedIds = mutableListOf<String>()
    val unfollowedFeedIds = mutableListOf<String>()

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

    override suspend fun getUserSettings(): UserSettings {
        getUserSettingsCallCount += 1
        nextGetSettingsFailure?.let { throwable ->
            nextGetSettingsFailure = null
            throw throwable
        }

        return currentSettings
    }

    override suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings {
        updates += patch
        nextUpdateFailure?.let { throwable ->
            nextUpdateFailure = null
            throw throwable
        }

        currentSettings = currentSettings.copy(
            darkMode = patch.darkMode ?: currentSettings.darkMode,
            suggestionsOutside = patch.suggestionsOutside ?: currentSettings.suggestionsOutside,
            minQualityScore = patch.minQualityScore ?: currentSettings.minQualityScore,
            language = patch.language ?: currentSettings.language,
            notificationsEnabled = patch.notificationsEnabled ?: currentSettings.notificationsEnabled,
            notificationsTime = patch.notificationsTime ?: currentSettings.notificationsTime,
            timezone = patch.timezone ?: currentSettings.timezone,
        )

        return currentSettings
    }

    override suspend fun getFollowedFeeds(): List<Feed> {
        getFollowedFeedsCallCount += 1
        nextGetFollowedFeedsFailure?.let { throwable ->
            nextGetFollowedFeedsFailure = null
            throw throwable
        }

        return currentFollowedFeeds
    }

    override suspend fun getAllFeeds(): List<Feed> {
        getAllFeedsCallCount += 1
        nextGetAllFeedsFailure?.let { throwable ->
            nextGetAllFeedsFailure = null
            throw throwable
        }

        return currentAllFeeds
    }

    override suspend fun followFeed(identifier: String) {
        followedFeedIds += identifier
        nextFollowFailure?.let { throwable ->
            nextFollowFailure = null
            throw throwable
        }

        val feed = currentAllFeeds.firstOrNull { it.id == identifier } ?: return
        if (currentFollowedFeeds.none { it.id == identifier }) {
            currentFollowedFeeds = currentFollowedFeeds + feed
        }
    }

    override suspend fun unfollowFeed(feedId: String) {
        unfollowedFeedIds += feedId
        nextUnfollowFailure?.let { throwable ->
            nextUnfollowFailure = null
            throw throwable
        }

        currentFollowedFeeds = currentFollowedFeeds.filterNot { it.id == feedId }
    }

    override suspend fun getCurrentUser(): CurrentUser {
        getCurrentUserCallCount += 1
        nextGetCurrentUserFailure?.let { throwable ->
            nextGetCurrentUserFailure = null
            throw throwable
        }

        return currentUser
    }

    override suspend fun getAuthSessions(): List<AuthSession> {
        getAuthSessionsCallCount += 1
        nextGetAuthSessionsFailure?.let { throwable ->
            nextGetAuthSessionsFailure = null
            throw throwable
        }

        return currentAuthSessions
    }

    override suspend fun terminateAuthSession(sessionId: String) {
        terminatedSessionIds += sessionId
        nextTerminateFailure?.let { throwable ->
            nextTerminateFailure = null
            throw throwable
        }

        currentAuthSessions = currentAuthSessions.filterNot { it.sessionId == sessionId }
    }

    override suspend fun getThreadHistory(threadId: String) =
        error("Not used in this test")

    override suspend fun sendGeneralChatMessage(message: String): String =
        error("Not used in this test")

    override suspend fun sendThreadChatMessage(threadId: String, message: String): String =
        error("Not used in this test")

    override suspend fun rateArticle(articleId: String, rating: Int) =
        error("Not used in this test")
}

private fun sampleUserSettings(): UserSettings = UserSettings(
    userId = "user-1",
    darkMode = true,
    suggestionsOutside = false,
    minQualityScore = 0.75,
    language = "pl",
    notificationsEnabled = true,
    notificationsTime = "8:45:00",
    timezone = "Europe/Warsaw",
    updatedAt = "2026-04-18T10:00:00Z",
)

private fun sampleCurrentUser(): CurrentUser = CurrentUser(
    id = "user-1",
    email = "hello@example.com",
    subscriptionTier = "pro",
    subscriptionExpiresAt = null,
    createdAt = "2026-04-18T09:00:00Z",
)

private fun sampleAuthSessions(): List<AuthSession> = listOf(
    AuthSession(
        sessionId = "session-1",
        deviceName = "MacBook Pro",
        platform = "desktop",
        city = "Torun",
        country = "Poland",
        lastSeenAt = "2026-04-18T09:00:00Z",
    ),
    AuthSession(
        sessionId = "session-2",
        deviceName = "Galaxy S25 Ultra",
        platform = "mobile",
        city = null,
        country = null,
        lastSeenAt = "2026-04-17T08:00:00Z",
    ),
)

private fun sampleFollowedFeeds(): List<Feed> = listOf(
    sampleFeed(
        id = "feed-followed-1",
        url = "https://www.wyborcza.pl/tech/rss.xml",
        language = "pl",
        tags = listOf("tech"),
    ),
    sampleFeed(
        id = "feed-followed-2",
        url = "https://www.nytimes.com/section/politics",
        language = "en",
        tags = listOf("politics"),
    ),
)

private fun sampleAllFeeds(): List<Feed> = sampleFollowedFeeds() + listOf(
    sampleFeed(
        id = "feed-available-1",
        url = "https://techcrunch.com/tag/ai/feed/",
        language = "en",
        tags = listOf("ai", "programming"),
    ),
    sampleFeed(
        id = "feed-available-2",
        url = "https://spidersweb.pl/mobile/feed/",
        language = "Polish",
        tags = listOf("mobile"),
    ),
)

private fun expectedAvailableFeeds(): List<Feed> {
    val followedIds = sampleFollowedFeeds().map(Feed::id).toSet()
    return sampleAllFeeds().filterNot { it.id in followedIds }
}

private fun sampleFeed(
    id: String,
    url: String,
    language: String,
    tags: List<String>,
): Feed = Feed(
    id = id,
    url = url,
    language = language,
    tags = tags,
    createdAt = "2026-04-18T09:00:00Z",
)

private fun expectedSettingsSessions(): List<SettingsSessionState> = listOf(
    SettingsSessionState(
        id = "session-1",
        title = "MacBook Pro",
        clientLabel = "Thulur Desktop",
        metaLabel = "Torun, Poland - 4/18/2026",
    ),
    SettingsSessionState(
        id = "session-2",
        title = "Galaxy S25 Ultra",
        clientLabel = "Thulur Mobile",
        metaLabel = "4/17/2026",
    ),
)

private fun expectedDisplayFeedSchedule(notificationsTime: String): FeedScheduleValue {
    val match = Regex("""(\d{1,2}):(\d{1,2})""").find(notificationsTime)
    val utcHour = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 8
    val utcMinute = match?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
    val systemTz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTz).date
    val utcDateTime = LocalDateTime(today, LocalTime(utcHour, utcMinute, 0, 0))
    val localDateTime = utcDateTime.toInstant(TimeZone.UTC).toLocalDateTime(systemTz)
    return FeedScheduleValue(hour = localDateTime.hour, minute = localDateTime.minute)
}

private fun expectedBackendTime(value: FeedScheduleValue): String {
    val systemTz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTz).date
    val localDateTime = LocalDateTime(today, LocalTime(value.hour, value.minute, 0, 0))
    val utcDateTime = localDateTime.toInstant(systemTz).toLocalDateTime(TimeZone.UTC)
    return "${utcDateTime.hour.toString().padStart(2, '0')}:${utcDateTime.minute.toString().padStart(2, '0')}:00.000Z"
}
