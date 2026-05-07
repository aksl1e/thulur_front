package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.presentation.composables.ThulurArticleItemVariant
import com.example.thulur.presentation.composables.ThulurThreadArticleData
import com.example.thulur.presentation.composables.extractSourceLabel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal data class DailyFeedPublishedMetadata(
    val dateText: String?,
    val timeText: String?,
)

internal fun Article.toThulurThreadArticleData(): ThulurThreadArticleData {
    val publishedMetadata = parsePublishedMetadata(published)

    return ThulurThreadArticleData(
        id = id,
        url = url,
        imageUrl = imageUrl,
        variant = toThulurArticleItemVariant(),
        isRead = isRead,
        title = title,
        summary = displaySummary,
        sourceLabel = extractSourceLabel(url),
        dateText = publishedMetadata.dateText,
        timeText = publishedMetadata.timeText,
        showDate = false,
    )
}

internal fun Article.toThulurArticleItemVariant(): ThulurArticleItemVariant = if (isRead) {
    ThulurArticleItemVariant.Read
} else {
    quality.toThulurArticleItemVariant()
}

internal fun ArticleQuality.toThulurArticleItemVariant(): ThulurArticleItemVariant = when (this) {
    ArticleQuality.Trash -> ThulurArticleItemVariant.Trash
    ArticleQuality.Default -> ThulurArticleItemVariant.Default
    ArticleQuality.Important -> ThulurArticleItemVariant.Important
}

internal fun parsePublishedMetadata(published: String?): DailyFeedPublishedMetadata {
    val dateTime = parsePublishedDateTime(published) ?: return DailyFeedPublishedMetadata(
        dateText = null,
        timeText = null,
    )

    return DailyFeedPublishedMetadata(
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
