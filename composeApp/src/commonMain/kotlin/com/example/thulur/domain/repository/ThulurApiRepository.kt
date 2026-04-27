package com.example.thulur.domain.repository

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import kotlinx.datetime.LocalDate

/**
 * App data boundary over the Thulur API module.
 *
 * The repository hides transport details from features and exposes
 * app-oriented models instead of raw DTOs.
 */
interface ThulurApiRepository {
    /**
     * Loads the Daily Feed for the current user.
     *
     * @param day Optional day filter. When `null`, backend default behavior is used.
     */
    suspend fun getDailyFeed(day: LocalDate? = null): DailyFeed

    /**
     * Loads paragraph metadata for a single article.
     */
    suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph>

    /**
     * Loads thread history grouped by day for a single thread.
     */
    suspend fun getThreadHistory(threadId: String): ThreadHistory

    /**
     * Loads settings for the current user.
     */
    suspend fun getUserSettings(): UserSettings

    /**
     * Applies a partial settings update for the current user.
     */
    suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings

    /**
     * Loads feeds followed by the current user.
     */
    suspend fun getFollowedFeeds(): List<Feed>

    /**
     * Loads all feeds visible to the app.
     */
    suspend fun getAllFeeds(): List<Feed>

    /**
     * Follows a single feed for the current user.
     */
    suspend fun followFeed(identifier: String)

    /**
     * Unfollows a single feed for the current user.
     */
    suspend fun unfollowFeed(feedId: String)

    /**
     * Loads the current authenticated user.
     */
    suspend fun getCurrentUser(): CurrentUser

    /**
     * Loads active auth sessions for the current user.
     */
    suspend fun getAuthSessions(): List<AuthSession>

    /**
     * Terminates a single auth session.
     */
    suspend fun terminateAuthSession(sessionId: String)

    /**
     * Submits a reading-quality rating for a single article.
     *
     * @param articleId Backend article identifier.
     * @param rating Integer score in [0, 10].
     */
    suspend fun rateArticle(articleId: String, rating: Int)
}
