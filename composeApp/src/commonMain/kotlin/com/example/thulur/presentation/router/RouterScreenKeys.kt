package com.example.thulur.presentation.router

import kotlinx.datetime.LocalDate

internal fun authScreenKey(): String = "auth"

internal fun rootLoadingScreenKey(): String = "root-loading"

internal fun dailyFeedScreenKey(sessionInstanceId: Int): String =
    "daily-feed-session-$sessionInstanceId"

internal fun settingsScreenKey(sessionInstanceId: Int): String =
    "settings-session-$sessionInstanceId"

internal fun threadHistoryScreenKey(
    sessionInstanceId: Int,
    threadId: String,
    initialDay: LocalDate,
): String = "thread-history-session-$sessionInstanceId-thread-$threadId-day-$initialDay"

internal fun chatScreenKey(sessionInstanceId: Int, openId: Int): String =
    "chat-session-$sessionInstanceId-open-$openId"

internal fun articleReaderScreenKey(sessionInstanceId: Int, articleId: String): String =
    "article-reader-session-$sessionInstanceId-article-$articleId"
