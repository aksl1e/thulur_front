package com.example.thulur_api.methods.daily_feed

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class DailyFeedMethodTest {
    @Test
    fun `does not send day query when day is null`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = HttpClient(
            engine = MockEngine {
                capturedRequest = it
                respond(
                    content = "[]",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )
            },
        ) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
        }

        DailyFeedMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals("/users/me/daily_feed", capturedRequest?.url?.encodedPath)
        assertNull(capturedRequest?.url?.parameters?.get("day"))
    }

    @Test
    fun `sends day query in yyyy-mm-dd format`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = HttpClient(
            engine = MockEngine {
                capturedRequest = it
                respond(
                    content = "[]",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )
            },
        ) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
        }

        DailyFeedMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            day = LocalDate(2026, 3, 23),
        )

        assertEquals("/users/me/daily_feed", capturedRequest?.url?.encodedPath)
        assertEquals("2026-03-23", capturedRequest?.url?.parameters?.get("day"))
    }

    @Test
    fun `deserializes current daily feed article shape and ignores unknown fields`() = runTest {
        val client = HttpClient(
            engine = MockEngine {
                respond(
                    content = """
                        [
                          {
                            "thread_id": "thread-1",
                            "thread_name": "Thread 1",
                            "topic_id": null,
                            "topic_name": null,
                            "main_feed_score": 0.8,
                            "thread_first_seen": "9999-12-31",
                            "thread_summary": null,
                            "articles": [
                              {
                                "article_id": "article-1",
                                "feed_id": "feed-1",
                                "title": "Article",
                                "url": "https://example.com",
                                "published": null,
                                "quality_tier": "important",
                                "novelty_paragraphs_ids": ["p1", "p2"],
                                "display_summary": "Visible summary",
                                "is_read": false,
                                "is_suggestion": false
                              }
                            ]
                          }
                        ]
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )
            },
        ) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
        }

        val response = DailyFeedMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals(1, response.size)
        assertEquals(1, response.first().articles.size)
        assertEquals("important", response.first().articles.first().qualityTier)
        assertEquals("Visible summary", response.first().articles.first().displaySummary)
    }
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
