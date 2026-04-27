package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.ArticleQuality
import com.example.thulur.presentation.composables.ThulurArticleItemVariant
import com.example.thulur.presentation.composables.extractSourceLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DailyFeedArticleMetadataTest {
    @Test
    fun `extractSourceLabel strips scheme and www`() {
        assertEquals(
            "wyborcza.pl",
            extractSourceLabel("https://www.wyborcza.pl/article?id=1"),
        )
    }

    @Test
    fun `extractSourceLabel ignores query fragment and port`() {
        assertEquals(
            "example.com",
            extractSourceLabel("https://www.example.com:8443/path?q=1#top"),
        )
    }

    @Test
    fun `extractSourceLabel returns null for blank host`() {
        assertNull(extractSourceLabel("   "))
    }

    @Test
    fun `parsePublishedMetadata formats date and time`() {
        val metadata = parsePublishedMetadata("2026-03-27T08:40:00")

        assertEquals("27.03.2026", metadata.dateText)
        assertEquals("8:40", metadata.timeText)
    }

    @Test
    fun `parsePublishedMetadata returns nulls for invalid value`() {
        val metadata = parsePublishedMetadata("not-a-date")

        assertNull(metadata.dateText)
        assertNull(metadata.timeText)
    }

    @Test
    fun `quality maps to ThulurArticleItemVariant`() {
        assertEquals(
            ThulurArticleItemVariant.Trash,
            ArticleQuality.Trash.toThulurArticleItemVariant(),
        )
        assertEquals(
            ThulurArticleItemVariant.Default,
            ArticleQuality.Default.toThulurArticleItemVariant(),
        )
        assertEquals(
            ThulurArticleItemVariant.Important,
            ArticleQuality.Important.toThulurArticleItemVariant(),
        )
    }
}
