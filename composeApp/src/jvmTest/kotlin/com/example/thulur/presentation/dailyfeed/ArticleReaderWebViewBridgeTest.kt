package com.example.thulur.presentation.dailyfeed

import com.example.thulur.presentation.dailyfeed.article_reader.parseArticleReaderBridgeMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ArticleReaderWebViewBridgeTest {
    @Test
    fun `bridge parser decodes progress event`() {
        val message = parseArticleReaderBridgeMessage(
            """{"type":"progress","data":{"value":0.75}}""",
        )

        assertNotNull(message)
        assertEquals("progress", message.type)
        assertEquals(0.75, message.data?.value)
    }

    @Test
    fun `bridge parser decodes ready event`() {
        val message = parseArticleReaderBridgeMessage(
            """{"type":"ready","data":{}}""",
        )

        assertNotNull(message)
        assertEquals("ready", message.type)
        assertEquals(null, message.data?.value)
    }
}
