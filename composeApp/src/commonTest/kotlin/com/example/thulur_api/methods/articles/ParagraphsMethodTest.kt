package com.example.thulur_api.methods.articles

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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class ParagraphsMethodTest {
    @Test
    fun `requests paragraphs for article id`() = runTest {
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

        ParagraphsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(articleId = "article-1")

        assertEquals("/users/me/articles/article-1/paragraphs", capturedRequest?.url?.encodedPath)
    }

    @Test
    fun `deserializes paragraph payload`() = runTest {
        val client = HttpClient(
            engine = MockEngine {
                respond(
                    content = """
                        [
                          {
                            "idx": 0,
                            "text": "First paragraph",
                            "is_novel": true
                          },
                          {
                            "idx": 1,
                            "text": "Second paragraph",
                            "is_novel": false
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

        val response = ParagraphsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(articleId = "article-1")

        assertEquals(2, response.size)
        assertEquals(0, response[0].idx)
        assertEquals("First paragraph", response[0].text)
        assertEquals(true, response[0].isNovel)
        assertEquals(1, response[1].idx)
        assertEquals("Second paragraph", response[1].text)
        assertEquals(false, response[1].isNovel)
    }
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
