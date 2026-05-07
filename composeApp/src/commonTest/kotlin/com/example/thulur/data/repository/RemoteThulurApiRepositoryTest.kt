package com.example.thulur.data.repository

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.AuthSessionDto
import com.example.thulur_api.dtos.ArticleDto
import com.example.thulur_api.dtos.DailyFeedDto
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.FeedDto
import com.example.thulur_api.dtos.ParagraphDto
import com.example.thulur_api.dtos.ThreadHistoryDayDto
import com.example.thulur_api.dtos.ThreadHistoryDto
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
                dailyFeed = dailyFeedDto(
                    isDefault = true,
                    threads = listOf(
                        DailyFeedThreadDto(
                            threadId = "thread-1",
                            threadName = "Thread 1",
                            topicId = "topic-1",
                            topicName = "Topic 1",
                            dailyFeedScore = 0.9,
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
            ),
        )

        val feed = repository.getDailyFeed()
        val thread = feed.threads.single()

        assertEquals(true, feed.isDefault)
        assertNull(thread.firstSeen)
        assertEquals(ArticleQuality.Trash, thread.articles[0].quality)
        assertEquals(ArticleQuality.Default, thread.articles[1].quality)
        assertEquals(ArticleQuality.Important, thread.articles[2].quality)
    }

    @Test
    fun `defaults quality tier when backend returns null or unknown value`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(
                    threads = listOf(
                        DailyFeedThreadDto(
                            threadId = "thread-1",
                            threadName = "Thread 1",
                            topicId = null,
                            topicName = null,
                            dailyFeedScore = 0.9,
                            threadFirstSeen = "9999-12-31",
                            threadSummary = "Summary",
                            articles = listOf(
                                article(qualityTier = null, id = "null-tier"),
                                article(qualityTier = "unexpected", id = "unknown-tier"),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val thread = repository.getDailyFeed().threads.single()

        assertEquals(ArticleQuality.Default, thread.articles[0].quality)
        assertEquals(ArticleQuality.Default, thread.articles[1].quality)
    }

    @Test
    fun `parses regular first seen date`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(
                    isDefault = false,
                    threads = listOf(
                        DailyFeedThreadDto(
                            threadId = "thread-1",
                            threadName = "Thread 1",
                            topicId = null,
                            topicName = null,
                            dailyFeedScore = 0.4,
                            threadFirstSeen = "2026-03-20",
                            threadSummary = null,
                            articles = listOf(article()),
                        ),
                    ),
                ),
            ),
        )

        val feed = repository.getDailyFeed()
        val thread = feed.threads.single()

        assertEquals(false, feed.isDefault)
        assertEquals(LocalDate(2026, 3, 20), thread.firstSeen)
    }

    @Test
    fun `maps daily feed article image url into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(
                    threads = listOf(
                        DailyFeedThreadDto(
                            threadId = "thread-1",
                            threadName = "Thread 1",
                            topicId = null,
                            topicName = null,
                            dailyFeedScore = 0.4,
                            threadFirstSeen = "2026-03-20",
                            threadSummary = null,
                            articles = listOf(
                                article(
                                    id = "article-1",
                                    imageUrl = "https://example.com/article-1.jpg",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val article = repository.getDailyFeed().threads.single().articles.single()

        assertEquals("https://example.com/article-1.jpg", article.imageUrl)
    }

    @Test
    fun `maps article paragraphs into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(),
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
    fun `maps thread history into app facing model and ignores legacy novelty fields`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(),
                history = ThreadHistoryDto(
                    threadId = "thread-1",
                    threadName = "Thread 1",
                    days = listOf(
                        ThreadHistoryDayDto(
                            day = "2026-04-17",
                            threadSummary = "Summary",
                            articles = listOf(
                                article(
                                    qualityTier = "important",
                                    id = "article-1",
                                    imageUrl = "https://example.com/article-1.jpg",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val history = repository.getThreadHistory(threadId = "thread-1")

        assertEquals("thread-1", history.threadId)
        assertEquals("Thread 1", history.threadName)
        assertEquals(LocalDate(2026, 4, 17), history.days.single().day)
        assertEquals("Summary", history.days.single().threadSummary)
        assertEquals(1, history.days.single().articles.size)
        assertEquals("article-1", history.days.single().articles.single().id)
        assertEquals(ArticleQuality.Important, history.days.single().articles.single().quality)
        assertEquals("Display summary", history.days.single().articles.single().displaySummary)
        assertEquals("https://example.com/article-1.jpg", history.days.single().articles.single().imageUrl)
    }

    @Test
    fun `maps user settings into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(),
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
            dailyFeed = dailyFeedDto(),
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
                dailyFeed = dailyFeedDto(),
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
        val api = FakeThulurApi(dailyFeed = dailyFeedDto())
        val repository = RemoteThulurApiRepository(thulurApi = api)

        repository.followFeed(identifier = "feed-1")
        repository.unfollowFeed(feedId = "feed-2")

        assertEquals("feed-1", api.followedFeedId)
        assertEquals("feed-2", api.unfollowedFeedId)
    }

    @Test
    fun `maps current user into app facing model`() = runTest {
        val repository = RemoteThulurApiRepository(
            thulurApi = FakeThulurApi(
                dailyFeed = dailyFeedDto(),
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
            dailyFeed = dailyFeedDto(),
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
    imageUrl: String? = null,
) = ArticleDto(
    articleId = id,
    feedId = "feed-1",
    title = "Title",
    url = "https://example.com/$id",
    imageUrl = imageUrl,
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

private fun dailyFeedDto(
    isDefault: Boolean = true,
    threads: List<DailyFeedThreadDto> = emptyList(),
) = DailyFeedDto(
    isDefault = isDefault,
    threads = threads,
)

private class FakeThulurApi(
    private val dailyFeed: DailyFeedDto,
    private val paragraphs: List<ParagraphDto> = emptyList(),
    private val settings: UserSettingsDto = userSettingsDto(),
    private val followedFeeds: List<FeedDto> = emptyList(),
    private val allFeeds: List<FeedDto> = emptyList(),
    private val currentUser: UserDto = userDto(),
    private val authSessions: List<AuthSessionDto> = emptyList(),
    private val history: ThreadHistoryDto = ThreadHistoryDto(
        threadId = "thread-1",
        threadName = "Thread 1",
        days = emptyList(),
    ),
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
    ): DailyFeedDto = dailyFeed

    override suspend fun getArticleParagraphs(
        articleId: String,
    ): List<ParagraphDto> = paragraphs

    override suspend fun getThreadHistory(
        threadId: String,
    ): ThreadHistoryDto = history

    override suspend fun getUserSettings(): UserSettingsDto = settings

    override suspend fun patchUserSettings(patch: UpdateUserSettingsDto): UserSettingsDto {
        updatedSettings = patch
        return settings
    }

    override suspend fun getFollowedFeeds(): List<FeedDto> = followedFeeds

    override suspend fun getAllFeeds(): List<FeedDto> = allFeeds

    override suspend fun followFeed(identifier: String) {
        followedFeedId = identifier
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

    override suspend fun rateArticle(articleId: String, rating: Int) =
        error("Not used in this test")
}
