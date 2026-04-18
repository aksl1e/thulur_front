package com.example.thulur_api.methods.thread_history

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class ThreadHistoryMethodTest {
    @Test
    fun `requests history for thread id`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = HttpClient(
            engine = MockEngine {
                capturedRequest = it
                respond(
                    content = """
                        {
                          "thread_id": "thread-1",
                          "thread_name": "Thread 1",
                          "days": []
                        }
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

        ThreadHistoryMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(threadId = "thread-1")

        assertEquals("/users/me/threads/thread-1/history", capturedRequest?.url?.encodedPath)
    }

    @Test
    fun `deserializes nested days and articles`() = runTest {
        val client = HttpClient(
            engine = MockEngine {
                respond(
                    content = """
                        {
                          "thread_id": "thread-1",
                          "thread_name": "Thread 1",
                          "days": [
                            {
                              "day": "2026-04-17",
                              "thread_summary": "Summary",
                              "articles": [
                                {
                                  "article_id": "article-1",
                                  "feed_id": "feed-1",
                                  "title": "Article",
                                  "url": "https://example.com/article-1",
                                  "published": "2026-04-17T08:00:00",
                                  "quality_score": 0.75,
                                  "novelty": true,
                                  "novelty_summary": "Legacy summary",
                                  "novelty_paragraphs_ids": ["p-1", "p-2"],
                                  "display_summary": "Visible summary",
                                  "is_read": false,
                                  "is_suggestion": true
                                }
                              ]
                            }
                          ]
                        }
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

        val response = ThreadHistoryMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(threadId = "thread-1")

        assertEquals("thread-1", response.threadId)
        assertEquals("Thread 1", response.threadName)
        assertEquals(1, response.days.size)
        assertEquals("2026-04-17", response.days.first().day)
        assertEquals("Summary", response.days.first().threadSummary)
        assertEquals(listOf("p-1", "p-2"), response.days.first().articles.first().noveltyParagraphsIds)
    }

    @Test
    fun `422 response still throws client request exception`() = runTest {
        val client = HttpClient(
            engine = MockEngine {
                respond(
                    content = """
                        {
                          "detail": [
                            {
                              "loc": ["path", 0],
                              "msg": "Invalid thread id",
                              "type": "value_error"
                            }
                          ]
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.UnprocessableEntity,
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

        val exception = assertFailsWith<ClientRequestException> {
            ThreadHistoryMethod(
                httpClient = client,
                config = ThulurApiConfig(),
            ).execute(threadId = "thread-1")
        }

        assertEquals(HttpStatusCode.UnprocessableEntity, exception.response.status)
    }
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
