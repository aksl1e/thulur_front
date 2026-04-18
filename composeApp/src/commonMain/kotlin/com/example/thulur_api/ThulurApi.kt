package com.example.thulur_api

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.AuthSessionDto
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.FeedDto
import com.example.thulur_api.dtos.ParagraphDto
import com.example.thulur_api.dtos.UpdateUserSettingsDto
import com.example.thulur_api.dtos.UserDto
import com.example.thulur_api.dtos.UserSettingsDto
import com.example.thulur_api.dtos.ThreadHistoryDto
import com.example.thulur_api.dtos.auth.AuthTokenDto
import com.example.thulur_api.dtos.auth.DesktopAuthMode
import com.example.thulur_api.dtos.auth.DesktopAuthStartDto
import com.example.thulur_api.methods.auth.GetAuthSessionsMethod
import com.example.thulur_api.methods.auth.TerminateAuthSessionMethod
import com.example.thulur_api.methods.auth.DesktopAuthExchangeMethod
import com.example.thulur_api.methods.auth.DesktopAuthStartMethod
import com.example.thulur_api.methods.articles.ParagraphsMethod
import com.example.thulur_api.methods.daily_feed.DailyFeedMethod
import com.example.thulur_api.methods.feeds.FollowFeedMethod
import com.example.thulur_api.methods.feeds.GetAllFeedsMethod
import com.example.thulur_api.methods.feeds.GetUserFeedsMethod
import com.example.thulur_api.methods.feeds.UnfollowFeedMethod
import com.example.thulur_api.methods.settings.GetSettingsMethod
import com.example.thulur_api.methods.settings.PatchSettingsMethod
import com.example.thulur_api.methods.users.GetCurrentUserMethod
import com.example.thulur_api.methods.thread_history.ThreadHistoryMethod
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

/**
 * Public transport boundary for all backend methods used by the app.
 *
 * The implementation is intentionally kept behind a single facade so the app
 * depends on one API boundary instead of wiring individual HTTP methods into
 * feature layers.
 */
interface ThulurApi {
    /**
     * Returns the raw daily feed payload for the bearer-authenticated user.
     *
     * @param day Optional day filter. When `null`, the backend resolves it to the current day.
     * @return Raw thread/article DTOs exactly as needed by the app data layer.
     */
    suspend fun getDailyFeed(
        day: LocalDate? = null,
    ): List<DailyFeedThreadDto>

    /**
     * Returns raw paragraph metadata for a single article.
     */
    suspend fun getArticleParagraphs(
        articleId: String,
    ): List<ParagraphDto>

    /**
     * Returns raw thread history for a single thread.
     */
    suspend fun getThreadHistory(
        threadId: String,
    ): ThreadHistoryDto

    /**
     * Returns the current user's settings.
     */
    suspend fun getUserSettings(): UserSettingsDto

    /**
     * Applies a partial settings update for the current user.
     */
    suspend fun patchUserSettings(
        patch: UpdateUserSettingsDto,
    ): UserSettingsDto

    /**
     * Returns feeds followed by the current user.
     */
    suspend fun getFollowedFeeds(): List<FeedDto>

    /**
     * Returns all available feeds.
     */
    suspend fun getAllFeeds(): List<FeedDto>

    /**
     * Follows a single feed for the current user.
     */
    suspend fun followFeed(feedId: String)

    /**
     * Unfollows a single feed for the current user.
     */
    suspend fun unfollowFeed(feedId: String)

    /**
     * Returns the current authenticated user.
     */
    suspend fun getCurrentUser(): UserDto

    /**
     * Returns active auth sessions for the current user.
     */
    suspend fun getAuthSessions(): List<AuthSessionDto>

    /**
     * Terminates a single auth session for the current user.
     */
    suspend fun terminateAuthSession(sessionId: String)

    suspend fun startDesktopAuth(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto

    suspend fun exchangeAuthCode(
        code: String,
        state: String,
        deviceName: String? = null,
        platform: String? = null,
    ): AuthTokenDto
}

/**
 * Remote Ktor-backed implementation of [ThulurApi].
 */
class RemoteThulurApi(
    httpClient: HttpClient,
    config: ThulurApiConfig,
) : ThulurApi {
    private val desktopAuthStartMethod = DesktopAuthStartMethod(
        httpClient = httpClient,
        config = config,
    )
    private val desktopAuthExchangeMethod = DesktopAuthExchangeMethod(
        httpClient = httpClient,
        config = config,
    )
    private val dailyFeedMethod = DailyFeedMethod(
        httpClient = httpClient,
        config = config,
    )
    private val paragraphsMethod = ParagraphsMethod(
        httpClient = httpClient,
        config = config,
    )
    private val threadHistoryMethod = ThreadHistoryMethod(
        httpClient = httpClient,
        config = config,
    )
    private val getSettingsMethod = GetSettingsMethod(
        httpClient = httpClient,
        config = config,
    )
    private val patchSettingsMethod = PatchSettingsMethod(
        httpClient = httpClient,
        config = config,
    )
    private val getUserFeedsMethod = GetUserFeedsMethod(
        httpClient = httpClient,
        config = config,
    )
    private val getAllFeedsMethod = GetAllFeedsMethod(
        httpClient = httpClient,
        config = config,
    )
    private val followFeedMethod = FollowFeedMethod(
        httpClient = httpClient,
        config = config,
    )
    private val unfollowFeedMethod = UnfollowFeedMethod(
        httpClient = httpClient,
        config = config,
    )
    private val getCurrentUserMethod = GetCurrentUserMethod(
        httpClient = httpClient,
        config = config,
    )
    private val getAuthSessionsMethod = GetAuthSessionsMethod(
        httpClient = httpClient,
        config = config,
    )
    private val terminateAuthSessionMethod = TerminateAuthSessionMethod(
        httpClient = httpClient,
        config = config,
    )

    override suspend fun getDailyFeed(
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = dailyFeedMethod.execute(
        day = day,
    )

    override suspend fun getArticleParagraphs(
        articleId: String,
    ): List<ParagraphDto> = paragraphsMethod.execute(
        articleId = articleId,
    )

    override suspend fun getThreadHistory(
        threadId: String,
    ): ThreadHistoryDto = threadHistoryMethod.execute(
        threadId = threadId,
    )

    override suspend fun getUserSettings(): UserSettingsDto = getSettingsMethod.execute()

    override suspend fun patchUserSettings(
        patch: UpdateUserSettingsDto,
    ): UserSettingsDto = patchSettingsMethod.execute(update = patch)

    override suspend fun getFollowedFeeds(): List<FeedDto> = getUserFeedsMethod.execute()

    override suspend fun getAllFeeds(): List<FeedDto> = getAllFeedsMethod.execute()

    override suspend fun followFeed(feedId: String) {
        followFeedMethod.execute(feedId = feedId)
    }

    override suspend fun unfollowFeed(feedId: String) {
        unfollowFeedMethod.execute(feedId = feedId)
    }

    override suspend fun getCurrentUser(): UserDto = getCurrentUserMethod.execute()

    override suspend fun getAuthSessions(): List<AuthSessionDto> = getAuthSessionsMethod.execute()

    override suspend fun terminateAuthSession(sessionId: String) {
        terminateAuthSessionMethod.execute(sessionId = sessionId)
    }

    override suspend fun startDesktopAuth(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto = desktopAuthStartMethod.execute(
        email = email,
        mode = mode,
        callbackUrl = callbackUrl,
        state = state,
    )

    override suspend fun exchangeAuthCode(
        code: String,
        state: String,
        deviceName: String?,
        platform: String?,
    ): AuthTokenDto = desktopAuthExchangeMethod.execute(
        code = code,
        state = state,
        deviceName = deviceName,
        platform = platform,
    )
}
