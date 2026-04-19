package com.example.thulur.data.repository

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.MainFeedArticle
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.AuthSessionDto
import com.example.thulur_api.dtos.DailyFeedArticleDto
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.FeedDto
import com.example.thulur_api.dtos.ParagraphDto
import com.example.thulur_api.dtos.UpdateUserSettingsDto
import com.example.thulur_api.dtos.UserDto
import com.example.thulur_api.dtos.UserSettingsDto
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
    fun `maps quality tiers and sentinel first seen`() = runTest {
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
                            article(qualityTier = "trash", id = "trash"),
                            article(qualityTier = "default", id = "default"),
                            article(qualityTier = "important", id = "important"),
                        ),
                    ),
                ),
            ),
        )

        val thread = repository.getMainFeed().single()

        assertNull(thread.firstSeen)
        assertEquals(MainFeedArticle.ArticleQuality.Trash, thread.articles[0].quality)
        assertEquals(MainFeedArticle.ArticleQuality.Default, thread.articles[1].quality)
        assertEquals(MainFeedArticle.ArticleQuality.Important, thread.articles[2].quality)
    }

    @Test
    fun `defaults quality tier when backend returns null or unknown value`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = listOf(
                    DailyFeedThreadDto(
                        threadId = "thread-1",
                        threadName = "Thread 1",
                        topicId = null,
                        topicName = null,
                        mainFeedScore = 0.9,
                        threadFirstSeen = "9999-12-31",
                        threadSummary = "Summary",
                        articles = listOf(
                            article(qualityTier = null, id = "null-tier"),
                            article(qualityTier = "unexpected", id = "unknown-tier"),
                        ),
                    ),
                ),
            ),
        )

        val thread = repository.getMainFeed().single()

        assertEquals(MainFeedArticle.ArticleQuality.Default, thread.articles[0].quality)
        assertEquals(MainFeedArticle.ArticleQuality.Default, thread.articles[1].quality)
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
                        articles = listOf(article()),
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
    fun `maps user settings into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = emptyList(),
                settings = userSettingsDto(),
            ),
        )

        val settings = repository.getUserSettings()

        assertEquals(
            UserSettings(
                userId = "user-1",
                darkMode = true,
                suggestionsOutside = false,
                minQualityScore = 0.7,
                language = "pl",
                notificationsEnabled = true,
                notificationsTime = "08:30:00",
                timezone = "Europe/Warsaw",
                updatedAt = "2026-04-18T09:15:00",
            ),
            settings,
        )
    }

    @Test
    fun `update user settings maps domain update to dto and returns mapped settings`() = runTest {
        val api = FakeThulurApi(
            threads = emptyList(),
            settings = userSettingsDto(),
        )
        val repository = RemoteThulurApiRepository(thulurApi = api)

        val response = repository.patchUserSettings(
            patch = PatchUserSettings(
                darkMode = true,
                minQualityScore = 0.8,
                timezone = "Europe/Warsaw",
            ),
        )

        assertEquals(
            UpdateUserSettingsDto(
                darkMode = true,
                minQualityScore = 0.8,
                timezone = "Europe/Warsaw",
            ),
            api.updatedSettings,
        )
        assertEquals("user-1", response.userId)
        assertEquals("Europe/Warsaw", response.timezone)
    }

    @Test
    fun `maps followed and all feeds into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = emptyList(),
                followedFeeds = listOf(feedDto(id = "followed-feed")),
                allFeeds = listOf(feedDto(id = "all-feed", language = null, tags = emptyList())),
            ),
        )

        val followedFeeds = repository.getFollowedFeeds()
        val allFeeds = repository.getAllFeeds()

        assertEquals(
            listOf(
                Feed(
                    id = "followed-feed",
                    url = "https://example.com/followed-feed.xml",
                    language = "en",
                    tags = listOf("ai", "news"),
                    createdAt = "2026-04-18T09:00:00",
                ),
            ),
            followedFeeds,
        )
        assertEquals(
            listOf(
                Feed(
                    id = "all-feed",
                    url = "https://example.com/all-feed.xml",
                    language = null,
                    tags = emptyList(),
                    createdAt = "2026-04-18T09:00:00",
                ),
            ),
            allFeeds,
        )
    }

    @Test
    fun `follow and unfollow feed delegate to transport`() = runTest {
        val api = FakeThulurApi(threads = emptyList())
        val repository = RemoteThulurApiRepository(thulurApi = api)

        repository.followFeed(feedId = "feed-1")
        repository.unfollowFeed(feedId = "feed-2")

        assertEquals("feed-1", api.followedFeedId)
        assertEquals("feed-2", api.unfollowedFeedId)
    }

    @Test
    fun `maps current user into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                threads = emptyList(),
                currentUser = userDto(),
            ),
        )

        val currentUser = repository.getCurrentUser()

        assertEquals(
            CurrentUser(
                id = "user-1",
                email = "hello@example.com",
                subscriptionTier = "pro",
                subscriptionExpiresAt = null,
                createdAt = "2026-04-18T09:00:00Z",
            ),
            currentUser,
        )
    }

    @Test
    fun `maps auth sessions into app facing model and delegates terminate`() = runTest {
        val api = FakeThulurApi(
            threads = emptyList(),
            authSessions = listOf(authSessionDto()),
        )
        val repository = RemoteThulurApiRepository(thulurApi = api)

        val sessions = repository.getAuthSessions()
        repository.terminateAuthSession(sessionId = "session-1")

        assertEquals(
            listOf(
                AuthSession(
                    sessionId = "session-1",
                    deviceName = "MacBook Pro",
                    platform = "desktop",
                    city = "Torun",
                    country = "Poland",
                    lastSeenAt = "2026-04-18T09:00:00Z",
                ),
            ),
            sessions,
        )
        assertEquals("session-1", api.terminatedSessionId)
    }
}

private fun article(
    qualityTier: String? = "default",
    id: String = "article",
) = DailyFeedArticleDto(
    articleId = id,
    feedId = "feed-1",
    title = "Title",
    url = "https://example.com/$id",
    published = null,
    qualityTier = qualityTier,
    displaySummary = "Display summary",
    isRead = false,
    isSuggestion = false,
)

private fun userSettingsDto() = UserSettingsDto(
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

private fun feedDto(
    id: String,
    language: String? = "en",
    tags: List<String> = listOf("ai", "news"),
) = FeedDto(
    id = id,
    url = "https://example.com/$id.xml",
    language = language,
    tags = tags,
    createdAt = "2026-04-18T09:00:00",
)

private fun userDto() = UserDto(
    id = "user-1",
    email = "hello@example.com",
    subscriptionTier = "pro",
    subscriptionExpiresAt = null,
    createdAt = "2026-04-18T09:00:00Z",
)

private fun authSessionDto() = AuthSessionDto(
    sessionId = "session-1",
    deviceName = "MacBook Pro",
    platform = "desktop",
    city = "Torun",
    country = "Poland",
    lastSeenAt = "2026-04-18T09:00:00Z",
)

private class FakeThulurApi(
    private val threads: List<DailyFeedThreadDto>,
    private val paragraphs: List<ParagraphDto> = emptyList(),
    private val settings: UserSettingsDto = userSettingsDto(),
    private val followedFeeds: List<FeedDto> = emptyList(),
    private val allFeeds: List<FeedDto> = emptyList(),
    private val currentUser: UserDto = userDto(),
    private val authSessions: List<AuthSessionDto> = emptyList(),
) : ThulurApi {
    var updatedSettings: UpdateUserSettingsDto? = null
        private set
    var followedFeedId: String? = null
        private set
    var unfollowedFeedId: String? = null
        private set
    var terminatedSessionId: String? = null
        private set

    override suspend fun getDailyFeed(
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = threads

    override suspend fun getArticleParagraphs(
        articleId: String,
    ): List<ParagraphDto> = paragraphs

    override suspend fun getUserSettings(): UserSettingsDto = settings

    override suspend fun patchUserSettings(patch: UpdateUserSettingsDto): UserSettingsDto {
        updatedSettings = patch
        return settings
    }

    override suspend fun getFollowedFeeds(): List<FeedDto> = followedFeeds

    override suspend fun getAllFeeds(): List<FeedDto> = allFeeds

    override suspend fun followFeed(feedId: String) {
        followedFeedId = feedId
    }

    override suspend fun unfollowFeed(feedId: String) {
        unfollowedFeedId = feedId
    }

    override suspend fun getCurrentUser(): UserDto = currentUser

    override suspend fun getAuthSessions(): List<AuthSessionDto> = authSessions

    override suspend fun terminateAuthSession(sessionId: String) {
        terminatedSessionId = sessionId
    }

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
