package com.example.thulur.presentation.chat.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatMarkdownParserTest {
    private val bodyStyle = TextStyle(fontFamily = FontFamily.SansSerif)
    private val codeStyle = TextStyle(fontFamily = FontFamily.Monospace)

    @Test
    fun `parses inline emphasis and code spans`() {
        val blocks = parseChatMarkdown(
            markdown = "plain **bold** *italic* ***both*** `code`",
            bodyStyle = bodyStyle,
            codeStyle = codeStyle,
            textColor = Color.Black,
            codeBackground = Color.White,
        )

        val paragraph = blocks.single() as ChatMarkdownBlock.Paragraph
        assertEquals("plain bold italic both code", paragraph.text.text)

        val spans = paragraph.text.spanStyles
        assertEquals("bold", paragraph.text.text.substring(spans.first { it.item.fontWeight == FontWeight.Bold && it.item.fontStyle != FontStyle.Italic }.start, spans.first { it.item.fontWeight == FontWeight.Bold && it.item.fontStyle != FontStyle.Italic }.end))
        assertEquals("italic", paragraph.text.text.substring(spans.first { it.item.fontStyle == FontStyle.Italic && it.item.fontWeight != FontWeight.Bold }.start, spans.first { it.item.fontStyle == FontStyle.Italic && it.item.fontWeight != FontWeight.Bold }.end))
        assertEquals("both", paragraph.text.text.substring(spans.first { it.item.fontStyle == FontStyle.Italic && it.item.fontWeight == FontWeight.Bold }.start, spans.first { it.item.fontStyle == FontStyle.Italic && it.item.fontWeight == FontWeight.Bold }.end))
        assertEquals("code", paragraph.text.text.substring(spans.first { it.item.fontFamily == FontFamily.Monospace }.start, spans.first { it.item.fontFamily == FontFamily.Monospace }.end))
    }

    @Test
    fun `parses paragraphs and unordered lists`() {
        val blocks = parseChatMarkdown(
            markdown = "First line\nSecond line\n\n- one\n- two",
            bodyStyle = bodyStyle,
            codeStyle = codeStyle,
            textColor = Color.Black,
            codeBackground = Color.White,
        )

        assertEquals(2, blocks.size)
        assertEquals(
            "First line\nSecond line",
            (blocks[0] as ChatMarkdownBlock.Paragraph).text.text,
        )
        assertEquals(
            listOf("one", "two"),
            (blocks[1] as ChatMarkdownBlock.BulletList).items.map { it.text },
        )
    }

    @Test
    fun `keeps unmatched markers as plain text`() {
        val blocks = parseChatMarkdown(
            markdown = "Keep *this open and `that",
            bodyStyle = bodyStyle,
            codeStyle = codeStyle,
            textColor = Color.Black,
            codeBackground = Color.White,
        )

        val paragraph = blocks.single() as ChatMarkdownBlock.Paragraph
        assertEquals("Keep *this open and `that", paragraph.text.text)
        assertTrue(paragraph.text.spanStyles.isEmpty())
    }
}
