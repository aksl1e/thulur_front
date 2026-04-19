package com.example.thulur.domain.usecase

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class SettingsAndFeedsUseCaseTest {
    @Test
    fun `get user settings delegates to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        val response = GetUserSettingsUseCase(repository)()

        assertEquals(1, repository.getUserSettingsCalls)
        assertEquals(sampleUserSettings(), response)
    }

    @Test
    fun `update user settings delegates update payload unchanged`() = runTest {
        val repository = SettingsFeedsTrackingRepository()
        val update = PatchUserSettings(
            darkMode = true,
            timezone = "Europe/Warsaw",
        )

        val response = PatchUserSettingsUseCase(repository)(update = update)

        assertEquals(update, repository.updatedSettings)
        assertEquals(sampleUserSettings(), response)
    }

    @Test
    fun `get followed feeds delegates to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        val response = GetFollowedFeedsUseCase(repository)()

        assertEquals(1, repository.getFollowedFeedsCalls)
        assertEquals(listOf(sampleFeed("followed-feed")), response)
    }

    @Test
    fun `get all feeds delegates to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        val response = GetAllFeedsUseCase(repository)()

        assertEquals(1, repository.getAllFeedsCalls)
        assertEquals(listOf(sampleFeed("all-feed")), response)
    }

    @Test
    fun `follow feed delegates id to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        FollowFeedUseCase(repository)(feedId = "feed-1")

        assertEquals("feed-1", repository.followedFeedId)
    }

    @Test
    fun `unfollow feed delegates id to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        UnfollowFeedUseCase(repository)(feedId = "feed-2")

        assertEquals("feed-2", repository.unfollowedFeedId)
    }

    @Test
    fun `get current user delegates to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        val response = GetCurrentUserUseCase(repository)()

        assertEquals(1, repository.getCurrentUserCalls)
        assertEquals(sampleCurrentUser(), response)
    }

    @Test
    fun `get auth sessions delegates to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        val response = GetAuthSessionsUseCase(repository)()

        assertEquals(1, repository.getAuthSessionsCalls)
        assertEquals(listOf(sampleAuthSession()), response)
    }

    @Test
    fun `terminate auth session delegates id to repository`() = runTest {
        val repository = SettingsFeedsTrackingRepository()

        TerminateAuthSessionUseCase(repository)(sessionId = "session-1")

        assertEquals("session-1", repository.terminatedSessionId)
    }
}

private class SettingsFeedsTrackingRepository : ThulurApiRepository {
    var getUserSettingsCalls = 0
    var getFollowedFeedsCalls = 0
    var getAllFeedsCalls = 0
    var getCurrentUserCalls = 0
    var getAuthSessionsCalls = 0
    var updatedSettings: PatchUserSettings? = null
    var followedFeedId: String? = null
    var unfollowedFeedId: String? = null
    var terminatedSessionId: String? = null

    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> =
        error("Not used in this test")

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        error("Not used in this test")

    override suspend fun getUserSettings(): UserSettings {
        getUserSettingsCalls += 1
        return sampleUserSettings()
    }

    override suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings {
        updatedSettings = patch
        return sampleUserSettings()
    }

    override suspend fun getFollowedFeeds(): List<Feed> {
        getFollowedFeedsCalls += 1
        return listOf(sampleFeed("followed-feed"))
    }

    override suspend fun getAllFeeds(): List<Feed> {
        getAllFeedsCalls += 1
        return listOf(sampleFeed("all-feed"))
    }

    override suspend fun followFeed(feedId: String) {
        followedFeedId = feedId
    }

    override suspend fun unfollowFeed(feedId: String) {
        unfollowedFeedId = feedId
    }

    override suspend fun getCurrentUser(): CurrentUser {
        getCurrentUserCalls += 1
        return sampleCurrentUser()
    }

    override suspend fun getAuthSessions(): List<AuthSession> {
        getAuthSessionsCalls += 1
        return listOf(sampleAuthSession())
    }

    override suspend fun terminateAuthSession(sessionId: String) {
        terminatedSessionId = sessionId
    }
}

private fun sampleUserSettings() = UserSettings(
    userId = "user-1",
    darkMode = true,
    suggestionsOutside = false,
    minQualityScore = 0.7,
    language = "pl",
    notificationsEnabled = true,
    notificationsTime = "08:30:00",
    timezone = "Europe/Warsaw",
    updatedAt = "2026-04-18T09:15:00",
)

private fun sampleFeed(id: String) = Feed(
    id = id,
    url = "https://example.com/$id.xml",
    language = "en",
    tags = listOf("ai"),
    createdAt = "2026-04-18T09:00:00",
)

private fun sampleCurrentUser() = CurrentUser(
    id = "user-1",
    email = "hello@example.com",
    subscriptionTier = "pro",
    subscriptionExpiresAt = null,
    createdAt = "2026-04-18T09:00:00Z",
)

private fun sampleAuthSession() = AuthSession(
    sessionId = "session-1",
    deviceName = "MacBook Pro",
    platform = "desktop",
    city = "Torun",
    country = "Poland",
    lastSeenAt = "2026-04-18T09:00:00Z",
)
