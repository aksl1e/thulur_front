package com.example.thulur.data.repository

import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.ThreadHistoryDay
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur_api.dtos.ArticleDto
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

private fun Double.toArticleQuality(): ArticleQuality = when {
    this < 0.33 -> ArticleQuality.Trash
    this < 0.66 -> ArticleQuality.Default
    else -> ArticleQuality.Important
}

private const val NOT_SHOWN_SENTINEL_DATE: String = "9999-12-31"
