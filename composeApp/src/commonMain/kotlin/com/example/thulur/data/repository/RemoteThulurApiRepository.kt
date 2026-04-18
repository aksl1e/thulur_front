package com.example.thulur.data.repository

import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.ThreadHistoryDay
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur_api.dtos.ArticleDto
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.AuthSessionDto
import com.example.thulur_api.dtos.FeedDto
import com.example.thulur_api.dtos.UpdateUserSettingsDto
import com.example.thulur_api.dtos.UserDto
import com.example.thulur_api.dtos.UserSettingsDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDate.Companion.parse

/**
 * Remote repository implementation backed by [ThulurApi].
 */
class RemoteThulurApiRepository(
    private val thulurApi: ThulurApi,
) : ThulurApiRepository {
    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> {
        return thulurApi
            .getDailyFeed(day = day)
            .map { threadDto ->
                MainFeedThread(
                    id = threadDto.threadId,
                    name = threadDto.threadName,
                    topicId = threadDto.topicId,
                    topicName = threadDto.topicName,
                    mainFeedScore = threadDto.mainFeedScore,
                    firstSeen = threadDto.threadFirstSeen.toMainFeedDateOrNull(),
                    summary = threadDto.threadSummary,
                    articles = threadDto.articles.map(ArticleDto::toArticle),
                )
            }
    }

    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> =
        thulurApi.getArticleParagraphs(articleId = articleId).map { paragraphDto ->
            ArticleParagraph(
                idx = paragraphDto.idx,
                text = paragraphDto.text,
                isNovel = paragraphDto.isNovel,
            )
        }

    override suspend fun getUserSettings(): UserSettings =
        thulurApi.getUserSettings().toDomain()

    override suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings =
        thulurApi.patchUserSettings(patch = patch.toDto()).toDomain()

    override suspend fun getFollowedFeeds(): List<Feed> =
        thulurApi.getFollowedFeeds().map(FeedDto::toDomain)

    override suspend fun getAllFeeds(): List<Feed> =
        thulurApi.getAllFeeds().map(FeedDto::toDomain)

    override suspend fun followFeed(feedId: String) {
        thulurApi.followFeed(feedId = feedId)
    }

    override suspend fun unfollowFeed(feedId: String) {
        thulurApi.unfollowFeed(feedId = feedId)
    }

    override suspend fun getCurrentUser(): CurrentUser =
        thulurApi.getCurrentUser().toDomain()

    override suspend fun getAuthSessions(): List<AuthSession> =
        thulurApi.getAuthSessions().map(AuthSessionDto::toDomain)

    override suspend fun terminateAuthSession(sessionId: String) {
        thulurApi.terminateAuthSession(sessionId = sessionId)
    }

    override suspend fun getThreadHistory(threadId: String): ThreadHistory =
        thulurApi.getThreadHistory(threadId = threadId).let { historyDto ->
            ThreadHistory(
                threadId = historyDto.threadId,
                threadName = historyDto.threadName,
                days = historyDto.days.map { dayDto ->
                    ThreadHistoryDay(
                        day = parse(dayDto.day),
                        threadSummary = dayDto.threadSummary,
                        articles = dayDto.articles.map(ArticleDto::toArticle),
                    )
                },
            )
        }
}

private fun String?.toMainFeedDateOrNull(): LocalDate? = when (this) {
    null,
    NOT_SHOWN_SENTINEL_DATE,
    -> null

    else -> parse(this)
}

private fun ArticleDto.toArticle(): Article = Article(
    id = articleId,
    feedId = feedId,
    title = title,
    url = url,
    published = published,
    displaySummary = displaySummary,
    isRead = isRead,
    isSuggestion = isSuggestion,
    quality = qualityScore.toArticleQuality(),
)

private fun String?.toArticleQuality(): ArticleQuality = when (this?.lowercase()) {
    "trash" -> ArticleQuality.Trash
    "important" -> MainFeedArticle.ArticleQuality.Important
    "default",
    null,
    -> ArticleQuality.Default

    else -> ArticleQuality.Default
}

private fun UserSettingsDto.toDomain(): UserSettings = UserSettings(
    userId = userId,
    darkMode = darkMode,
    suggestionsOutside = suggestionsOutside,
    minQualityScore = minQualityScore,
    language = language,
    notificationsEnabled = notificationsEnabled,
    notificationsTime = notificationsTime,
    timezone = timezone,
    updatedAt = updatedAt,
)

private fun PatchUserSettings.toDto(): UpdateUserSettingsDto = UpdateUserSettingsDto(
    darkMode = darkMode,
    suggestionsOutside = suggestionsOutside,
    minQualityScore = minQualityScore,
    language = language,
    notificationsEnabled = notificationsEnabled,
    notificationsTime = notificationsTime,
    timezone = timezone,
)

private fun FeedDto.toDomain(): Feed = Feed(
    id = id,
    url = url,
    language = language,
    tags = tags,
    createdAt = createdAt,
)

private fun UserDto.toDomain(): CurrentUser = CurrentUser(
    id = id,
    email = email,
    subscriptionTier = subscriptionTier,
    subscriptionExpiresAt = subscriptionExpiresAt,
    createdAt = createdAt,
)

private fun AuthSessionDto.toDomain(): AuthSession = AuthSession(
    sessionId = sessionId,
    deviceName = deviceName,
    platform = platform,
    city = city,
    country = country,
    lastSeenAt = lastSeenAt,
)

private const val NOT_SHOWN_SENTINEL_DATE: String = "9999-12-31"
