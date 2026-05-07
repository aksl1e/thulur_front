package com.example.thulur.domain.session

import kotlinx.coroutines.flow.StateFlow

interface ReadArticlesCache {
    val readArticles: StateFlow<Map<String, Boolean>>

    fun isRead(articleId: String): Boolean

    fun markRead(articleId: String)

    fun clear()
}
