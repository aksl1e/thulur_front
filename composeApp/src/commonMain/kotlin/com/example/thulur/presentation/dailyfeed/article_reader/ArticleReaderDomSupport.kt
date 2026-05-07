package com.example.thulur.presentation.dailyfeed.article_reader

import com.example.thulur.domain.model.ArticleParagraph
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs

private val articleReaderJson = Json {
    prettyPrint = false
}

internal fun normalizeArticleParagraphText(text: String): String = text
    .replace("\u00ad", "")      // soft hyphen
    .replace("\u200b", "")      // zero-width space
    .replace("\u200c", "")      // zero-width non-joiner
    .replace("\u200d", "")      // zero-width joiner
    .replace("\u00a0", " ")     // non-breaking space → regular space
    .replace("\u2018", "'")     // left single quotation mark
    .replace("\u2019", "'")     // right single quotation mark
    .replace("\u201c", "\"")    // left double quotation mark
    .replace("\u201d", "\"")    // right double quotation mark
    .replace("\u2013", " ")     // en dash → space
    .replace("\u2014", " ")     // em dash → space
    .replace("\u2026", "...")    // horizontal ellipsis → three dots
    .replace(Regex("\\s+"), " ")
    .trim()

internal fun orderArticleReaderCandidateIndices(
    candidateIndices: Iterable<Int>,
    paragraphIdx: Int,
): List<Int> = candidateIndices.sortedBy { candidateIndex ->
    abs(candidateIndex - paragraphIdx)
}

internal fun buildArticleReaderInjectionScript(
    paragraphs: List<ArticleParagraph>,
    includeRateTracker: Boolean = false,
): String {
    val payload = paragraphs.map { paragraph ->
        ArticleReaderParagraphPayload(
            idx = paragraph.idx,
            text = normalizeArticleParagraphText(paragraph.text),
            isNovel = paragraph.isNovel,
        )
    }
    val payloadJson = articleReaderJson.encodeToString(payload)

    return """
        (function() {
          const payload = $payloadJson;

          const sendEvent = function(type, data) {
            if (typeof window.cefQuery !== 'function') return;
            window.cefQuery({
              request: JSON.stringify({ type: type, data: data || {} }),
              onSuccess: function() {},
              onFailure: function() {}
            });
          };

          const normalizeText = function(value) {
            return (value || '')
              .replace(/\u00ad/g, '')
              .replace(/\u200b/g, '')
              .replace(/\u200c/g, '')
              .replace(/\u200d/g, '')
              .replace(/\u00a0/g, ' ')
              .replace(/[\u2018\u2019]/g, "'")
              .replace(/[\u201c\u201d]/g, '"')
              .replace(/[\u2013\u2014]/g, ' ')
              .replace(/\u2026/g, '...')
              .replace(/\s+/g, ' ')
              .trim();
          };

          // Collect paragraph candidates from the page.
          // Priority 1: semantic data-component/data-block attributes (BBC, Guardian, etc.)
          // Priority 2: article container + all <p> tags (generic fallback)
          const collectCandidates = function() {
            var specificSelectors = [
              '[data-component="text-block"] p',
              '[data-component="paragraph"] p',
              '[data-testid="paragraph"] p',
              '[data-block-type="paragraph"] p',
              '[data-type="text"] p'
            ];
            for (var s = 0; s < specificSelectors.length; s++) {
              var found = Array.from(document.querySelectorAll(specificSelectors[s]));
              if (found.length >= 4) {
                return found
                  .map(function(el, i) {
                    return { element: el, domIndex: i, text: normalizeText(el.innerText || el.textContent || '') };
                  })
                  .filter(function(c) { return c.text.length > 15; });
              }
            }

            var containerSelectors = [
              'article',
              '[role="article"]',
              '[itemprop="articleBody"]',
              '[class*="article-body"]',
              '[class*="article-content"]',
              '[class*="articleBody"]',
              '[class*="story-body"]',
              '[class*="story-content"]',
              '[class*="post-body"]',
              '[class*="post-content"]',
              '[class*="entry-content"]',
              '[class*="content-body"]',
              'main',
              '[role="main"]'
            ];
            var container = document.body;
            for (var i = 0; i < containerSelectors.length; i++) {
              var el = document.querySelector(containerSelectors[i]);
              if (el) { container = el; break; }
            }
            return Array.from(container.querySelectorAll('p'))
              .map(function(el, i) {
                return { element: el, domIndex: i, text: normalizeText(el.innerText || el.textContent || '') };
              })
              .filter(function(c) { return c.text.length > 15; });
          };

          // F1-based similarity: harmonic mean of recall (backend→DOM) and precision (DOM→backend).
          // This correctly handles the common case where DOM text is longer than backend text
          // (extra words from inline links, annotations, etc.) without penalising the score.
          // All comparisons are case-insensitive.
          const computeSimilarity = function(aRaw, bRaw) {
            var a = aRaw.toLowerCase();
            var b = bRaw.toLowerCase();
            if (a === b) return 1.0;
            var minLen = Math.min(a.length, b.length);
            if (minLen === 0) return 0;

            // Prefix match
            var prefixLen = Math.min(80, minLen);
            if (a.substring(0, prefixLen) === b.substring(0, prefixLen)) return 0.9;

            // Substring containment for substantial texts (handles partial extractions)
            if (minLen >= 50) {
              var shorter = a.length <= b.length ? a : b;
              var longer  = a.length <= b.length ? b : a;
              if (longer.indexOf(shorter) !== -1) return 0.92;
              var shortPrefix = shorter.substring(0, Math.min(60, shorter.length));
              if (longer.indexOf(shortPrefix) !== -1) return 0.85;
            }

            // Word-overlap F1
            var wordsA = a.split(' ').filter(function(w) { return w.length > 2; });
            var bWordsSet = {};
            var wordsB = b.split(' ').filter(function(w) { return w.length > 2; });
            wordsB.forEach(function(w) { bWordsSet[w] = true; });
            if (wordsA.length === 0 || wordsB.length === 0) return 0;

            var matches = wordsA.filter(function(w) { return bWordsSet[w]; }).length;
            if (matches < 3) return 0;

            var recall    = matches / wordsA.length;   // fraction of backend words found in DOM
            var precision = matches / wordsB.length;   // fraction of DOM words found in backend
            return (2 * recall * precision) / (recall + precision);
          };

          var SIMILARITY_THRESHOLD = 0.60;
          var candidates = collectCandidates();

          // Stage 1 — sequential forward matching (preserves document order).
          // For each backend paragraph, find the best match strictly after the previous match.
          var lastMatchedDomIndex = -1;
          var matchedDomIndices = {};
          var orderedMatches = [];
          var unmatchedPayload = [];

          payload.forEach(function(paragraph) {
            var bestScore = 0;
            var bestCandidate = null;
            var bestDomIndex = -1;

            for (var i = lastMatchedDomIndex + 1; i < candidates.length; i++) {
              var score = computeSimilarity(candidates[i].text, paragraph.text);
              if (score >= SIMILARITY_THRESHOLD && score > bestScore) {
                bestScore = score;
                bestCandidate = candidates[i];
                bestDomIndex = i;
                if (bestScore >= 0.9) break;
              }
            }

            if (bestCandidate) {
              lastMatchedDomIndex = bestDomIndex;
              matchedDomIndices[bestDomIndex] = true;
              orderedMatches.push({ domIndex: bestDomIndex, element: bestCandidate.element, isNovel: paragraph.isNovel });
            } else {
              unmatchedPayload.push(paragraph);
            }
          });

          // Stage 2 — global fallback for paragraphs that Stage 1 could not place.
          // Searches all unused candidates without the forward constraint, then re-sorts
          // by DOM index so the final list stays in document order.
          if (unmatchedPayload.length > 0) {
            unmatchedPayload.forEach(function(paragraph) {
              var bestScore = 0;
              var bestCandidate = null;
              var bestDomIndex = -1;

              for (var i = 0; i < candidates.length; i++) {
                if (matchedDomIndices[i]) continue;
                var score = computeSimilarity(candidates[i].text, paragraph.text);
                if (score >= SIMILARITY_THRESHOLD && score > bestScore) {
                  bestScore = score;
                  bestCandidate = candidates[i];
                  bestDomIndex = i;
                  if (bestScore >= 0.9) break;
                }
              }

              if (bestCandidate) {
                matchedDomIndices[bestDomIndex] = true;
                orderedMatches.push({ domIndex: bestDomIndex, element: bestCandidate.element, isNovel: paragraph.isNovel });
              }
            });

            orderedMatches.sort(function(a, b) { return a.domIndex - b.domIndex; });
          }

          var matchedElements = orderedMatches;

          const getPageBackground = function() {
            var bgNodes = [document.body, document.documentElement];
            for (var i = 0; i < bgNodes.length; i++) {
              if (!bgNodes[i]) continue;
              var bg = window.getComputedStyle(bgNodes[i]).backgroundColor;
              if (bg && bg !== 'rgba(0, 0, 0, 0)' && bg !== 'transparent') return bg;
            }
            return 'rgb(255, 255, 255)';
          };

          const parseRgb = function(colorStr) {
            var m = colorStr.match(/rgba?\(\s*(\d+),\s*(\d+),\s*(\d+)/);
            return m ? [parseInt(m[1]), parseInt(m[2]), parseInt(m[3])] : null;
          };

          const applyHighlight = function(element) {
            var textColor = window.getComputedStyle(element).color;
            var pageBgRgb = parseRgb(getPageBackground());
            var bgColor;

            if (pageBgRgb) {
              var lum = (0.299 * pageBgRgb[0] + 0.587 * pageBgRgb[1] + 0.114 * pageBgRgb[2]) / 255;
              if (lum < 0.5) {
                // Dark page — lighten background toward white
                var r = Math.round(pageBgRgb[0] + (255 - pageBgRgb[0]) * 0.22);
                var g = Math.round(pageBgRgb[1] + (255 - pageBgRgb[1]) * 0.22);
                var b = Math.round(pageBgRgb[2] + (255 - pageBgRgb[2]) * 0.22);
                bgColor = 'rgba(' + r + ', ' + g + ', ' + b + ', 0.75)';
              } else {
                // Light page — use text color as a very subtle tint
                var textRgb = parseRgb(textColor);
                bgColor = textRgb
                  ? 'rgba(' + textRgb[0] + ', ' + textRgb[1] + ', ' + textRgb[2] + ', 0.05)'
                  : 'rgba(' + pageBgRgb[0] + ', ' + pageBgRgb[1] + ', ' + pageBgRgb[2] + ', 0.5)';
              }
            } else {
              bgColor = 'rgba(255, 255, 255, 0.10)';
            }

            element.style.border = '2px solid ' + textColor;
            element.style.borderRadius = '12px';
            element.style.padding = '12px 14px';
            element.style.boxSizing = 'border-box';
            element.style.backgroundColor = bgColor;
          };

          matchedElements.forEach(function(match) {
            if (match.isNovel) {
              applyHighlight(match.element);
            }
          });

          var readingElements = matchedElements.length > 0
            ? matchedElements.map(function(m) { return m.element; })
            : candidates.map(function(c) { return c.element; });

          var initialScrollY = window.scrollY;
          var maxProgress = 0;
          var lastSentAt = 0;

          var computeProgress = function() {
            var now = Date.now();
            if (now - lastSentAt < 60) return;
            lastSentAt = now;

            var startElement = readingElements[0];
            var endElement = readingElements[readingElements.length - 1];
            var documentHeight = Math.max(
              document.documentElement.scrollHeight,
              document.body ? document.body.scrollHeight : 0
            );
            var progress = 0;

            if (startElement && endElement) {
              var contentStart = startElement.getBoundingClientRect().top + window.scrollY;
              var contentEnd = endElement.getBoundingClientRect().bottom + window.scrollY;
              var effectiveStart = Math.max(initialScrollY, contentStart);
              var maxDistance = Math.max(1, contentEnd - window.innerHeight - effectiveStart);
              var currentDistance = Math.max(0, window.scrollY - effectiveStart);
              progress = Math.max(0, Math.min(1, currentDistance / maxDistance));
              if (window.scrollY + window.innerHeight >= contentEnd - 8) {
                progress = 1;
              }
            } else {
              var maxDistance2 = Math.max(1, documentHeight - window.innerHeight - initialScrollY);
              var currentDistance2 = Math.max(0, window.scrollY - initialScrollY);
              progress = Math.max(0, Math.min(1, currentDistance2 / maxDistance2));
            }

            if (progress > maxProgress) {
              maxProgress = progress;
            }

            sendEvent('progress', { value: maxProgress });
          };

          window.addEventListener('scroll', computeProgress, { passive: true });

          ${if (includeRateTracker) rateTrackerJs else ""}

          computeProgress();
          sendEvent('ready', { matchedCount: matchedElements.length });
        })();
    """.trimIndent()
}

private val rateTrackerJs = """
          var rateContainer = (function() {
            var selectors = [
              'article', '[role="article"]', '[itemprop="articleBody"]',
              '[class*="article-body"]', '[class*="article-content"]',
              '[class*="articleBody"]', '[class*="story-body"]',
              '[class*="story-content"]', '[class*="post-body"]',
              '[class*="post-content"]', '[class*="entry-content"]',
              '[class*="content-body"]', 'main', '[role="main"]'
            ];
            for (var si = 0; si < selectors.length; si++) {
              var el = document.querySelector(selectors[si]);
              if (el) return el;
            }
            return document.body;
          })();

          var rateWordCount = ((rateContainer.innerText || '').match(/\S+/g) || []).length; // get all article's words
          var readingSpeed = 200; // reading speed - words per minute
          var rateTimePerChunk = Math.max(2, rateWordCount * 6 / readingSpeed);
          var rateChunkTime = new Array(10).fill(0);
          var rateChunkDone = new Array(10).fill(false);
          var rateLastSent = -1;

          var sendRate = function() {
            var total = 0;
            for (var ci = 0; ci < 10; ci++) { if (rateChunkDone[ci]) total++; }
            if (total !== rateLastSent) {
              rateLastSent = total;
              sendEvent('rate', { rate: total });
            }
          };

          setInterval(function() {
            if (document.hidden) return;
            var rect = rateContainer.getBoundingClientRect();
            if (rect.height === 0) return;
            var vh = window.innerHeight;
            var topPct  = Math.max(0, -rect.top) / rect.height;
            var botPct  = Math.min(rect.height, vh - rect.top) / rect.height;
            for (var ci = 0; ci < 10; ci++) {
              if (botPct > ci / 10 && topPct < (ci + 1) / 10) {
                rateChunkTime[ci]++;
                if (rateChunkTime[ci] >= rateTimePerChunk) rateChunkDone[ci] = true;
              }
            }
            sendRate();
          }, 1000);

          window.addEventListener('beforeunload', sendRate);
""".trimIndent()

@Serializable
private data class ArticleReaderParagraphPayload(
    val idx: Int,
    val text: String,
    val isNovel: Boolean,
)
