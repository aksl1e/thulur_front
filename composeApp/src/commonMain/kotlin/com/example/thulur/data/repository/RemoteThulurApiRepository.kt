package com.example.thulur.data.repository

import com.example.thulur.domain.model.MainFeedArticle
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur_api.ThulurApi
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
                    articles = threadDto.articles.map { articleDto ->
                        MainFeedArticle(
                            id = articleDto.articleId,
                            feedId = articleDto.feedId,
                            title = articleDto.title,
                            url = articleDto.url,
                            published = articleDto.published,
                            displaySummary = articleDto.displaySummary,
                            isRead = articleDto.isRead,
                            isSuggestion = articleDto.isSuggestion,
                            quality = articleDto.qualityScore.toArticleQuality(),
                        )
                    },
                )
            }
    }
}

private fun String?.toMainFeedDateOrNull(): LocalDate? = when (this) {
    null,
    NOT_SHOWN_SENTINEL_DATE,
    -> null

    else -> parse(this)
}

private fun Double.toArticleQuality(): MainFeedArticle.ArticleQuality = when {
    this < 0.33 -> MainFeedArticle.ArticleQuality.Trash
    this < 0.66 -> MainFeedArticle.ArticleQuality.Default
    else -> MainFeedArticle.ArticleQuality.Important
}

private const val NOT_SHOWN_SENTINEL_DATE: String = "9999-12-31"
