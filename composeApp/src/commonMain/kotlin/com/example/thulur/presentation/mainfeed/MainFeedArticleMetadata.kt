package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.MainFeedArticle
import com.example.thulur.presentation.composables.ThulurArticleItemVariant
import com.example.thulur.presentation.composables.ThulurThreadArticleData
import com.example.thulur.presentation.composables.extractSourceLabel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal data class MainFeedPublishedMetadata(
    val dateText: String?,
    val timeText: String?,
)

internal fun MainFeedArticle.toThulurThreadArticleData(): ThulurThreadArticleData {
    val publishedMetadata = parsePublishedMetadata(published)

    return ThulurThreadArticleData(
        id = id,
        url = url,
        variant = quality.toThulurArticleItemVariant(),
        title = title,
        summary = displaySummary,
        sourceLabel = extractSourceLabel(url),
        dateText = publishedMetadata.dateText,
        timeText = publishedMetadata.timeText,
        showDate = false,
    )
}

internal fun MainFeedArticle.ArticleQuality.toThulurArticleItemVariant(): ThulurArticleItemVariant = when (this) {
    MainFeedArticle.ArticleQuality.Trash -> ThulurArticleItemVariant.Trash
    MainFeedArticle.ArticleQuality.Default -> ThulurArticleItemVariant.Default
    MainFeedArticle.ArticleQuality.Important -> ThulurArticleItemVariant.Important
}

internal fun parsePublishedMetadata(published: String?): MainFeedPublishedMetadata {
    val dateTime = parsePublishedDateTime(published) ?: return MainFeedPublishedMetadata(
        dateText = null,
        timeText = null,
    )

    return MainFeedPublishedMetadata(
        dateText = dateTime.toDateLabel(),
        timeText = dateTime.toTimeLabel(),
    )
}

private fun parsePublishedDateTime(published: String?): LocalDateTime? {
    if (published.isNullOrBlank()) return null

    return runCatching {
        Instant.parse(published).toLocalDateTime(TimeZone.currentSystemDefault())
    }.getOrNull() ?: runCatching {
        LocalDateTime.parse(published)
    }.getOrNull()
}

private fun LocalDateTime.toDateLabel(): String {
    val day = date.day.toString().padStart(2, '0')
    val month = (date.month.ordinal + 1).toString().padStart(2, '0')

    return "$day.$month.${date.year}"
}

private fun LocalDateTime.toTimeLabel(): String {
    val minute = minute.toString().padStart(2, '0')

    return "$hour:$minute"
}
