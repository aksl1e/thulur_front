package com.example.thulur.presentation.dailyfeed

import com.example.thulur.presentation.router.ArticleReaderScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ArticleReaderScreenKeyTest {
    @Test
    fun `generates stable key for same session and article`() {
        assertEquals(
            "article-reader-session-5-article-article-1",
            ArticleReaderScreen(
                sessionInstanceId = 5,
                articleId = "article-1",
                title = "Article 1",
                url = "https://example.com/articles/1",
                isRead = false,
            ).key,
        )
    }

    @Test
    fun `generates different keys for different articles`() {
        assertNotEquals(
            ArticleReaderScreen(
                sessionInstanceId = 5,
                articleId = "article-1",
                title = "Article 1",
                url = "https://example.com/articles/1",
                isRead = false,
            ).key,
            ArticleReaderScreen(
                sessionInstanceId = 5,
                articleId = "article-2",
                title = "Article 2",
                url = "https://example.com/articles/2",
                isRead = false,
            ).key,
        )
    }
}
