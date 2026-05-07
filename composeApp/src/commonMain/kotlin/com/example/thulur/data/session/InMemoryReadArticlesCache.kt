package com.example.thulur.data.session

import com.example.thulur.domain.session.ReadArticlesCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryReadArticlesCache : ReadArticlesCache {
    private val _readArticles = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override val readArticles: StateFlow<Map<String, Boolean>> = _readArticles.asStateFlow()

    override fun isRead(articleId: String): Boolean = _readArticles.value[articleId] == true

    override fun markRead(articleId: String) {
        val normalizedArticleId = articleId.trim()
        if (normalizedArticleId.isBlank()) return

        _readArticles.update { current ->
            if (current[normalizedArticleId] == true) current else current + (normalizedArticleId to true)
        }
    }

    override fun clear() {
        if (_readArticles.value.isEmpty()) return
        _readArticles.value = emptyMap()
    }
}
