package com.example.thulur.presentation.dailyfeed

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.presentation.dailyfeed.article_reader.buildArticleReaderInjectionScript
import com.example.thulur.presentation.dailyfeed.article_reader.normalizeArticleParagraphText
import com.example.thulur.presentation.dailyfeed.article_reader.orderArticleReaderCandidateIndices
import kotlin.test.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArticleReaderDomSupportTest {
    @Test
    fun `normalize paragraph text collapses repeated whitespace`() {
        assertEquals(
            "First second third",
            normalizeArticleParagraphText(" First \n  second\tthird "),
        )
    }

    @Test
    fun `candidate ordering prefers indices closest to backend idx`() {
        assertEquals(
            listOf(5, 7, 9),
            orderArticleReaderCandidateIndices(
                candidateIndices = listOf(9, 5, 7),
                paragraphIdx = 6,
            ),
        )
    }

    @Test
    fun `injection script contains normalized payload and matching strategy`() {
        val script = buildArticleReaderInjectionScript(
            paragraphs = listOf(
                ArticleParagraph(
                    idx = 4,
                    text = "Alpha \n beta",
                    isNovel = true,
                ),
            ),
        )

        assertTrue(script.contains("Alpha beta"))
        assertTrue(script.contains("computeSimilarity(candidates[i].text, paragraph.text)"))
        assertTrue(script.contains("lastMatchedDomIndex"))
        assertTrue(script.contains("orderedMatches.sort(function(a, b) { return a.domIndex - b.domIndex; })"))
        assertTrue(script.contains("const computeMutedTextColor = function(element)"))
        assertTrue(script.contains("element.style.color = computeMutedTextColor(element);"))
        assertTrue(script.contains("} else {"))
        assertTrue(script.contains("applyMutedText(match.element);"))
    }

    @Test
    fun `injection script omits rate tracker when disabled`() {
        val script = buildArticleReaderInjectionScript(
            paragraphs = listOf(
                ArticleParagraph(
                    idx = 1,
                    text = "Already read paragraph",
                    isNovel = false,
                ),
            ),
            includeRateTracker = false,
        )

        assertFalse(script.contains("var rateContainer = (function()"))
        assertFalse(script.contains("sendEvent('rate', { rate: total });"))
        assertTrue(script.contains("sendEvent('ready', { matchedCount: matchedElements.length });"))
    }
}
