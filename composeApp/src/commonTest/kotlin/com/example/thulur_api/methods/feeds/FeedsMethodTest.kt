package com.example.thulur_api.methods.feeds

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class FeedsMethodTest {
    @Test
    fun `get user feeds requests correct path and deserializes defaults`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    [
                      {
                        "id": "feed-1",
                        "url": "https://example.com/rss.xml",
                        "created_at": "2026-04-18T09:00:00"
                      }
                    ]
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = GetUserFeedsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/users/me/feeds", capturedRequest?.url?.encodedPath)
        assertEquals(1, response.size)
        assertEquals("feed-1", response.single().id)
        assertNull(response.single().language)
        assertEquals(emptyList(), response.single().tags)
    }

    @Test
    fun `get all feeds requests public path and deserializes values`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    [
                      {
                        "id": "feed-2",
                        "url": "https://example.com/pl.xml",
                        "language": "pl",
                        "tags": ["ai", "news"],
                        "created_at": "2026-04-18T09:05:00"
                      }
                    ]
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = GetAllFeedsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/feeds", capturedRequest?.url?.encodedPath)
        assertEquals("feed-2", response.single().id)
        assertEquals("pl", response.single().language)
        assertEquals(listOf("ai", "news"), response.single().tags)
    }

    @Test
    fun `follow feed uses post with identifier body and accepts no content response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = "",
                status = HttpStatusCode.NoContent,
            )
        }

        FollowFeedMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(identifier = "feed-3")

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/users/me/feeds/follow", capturedRequest?.url?.encodedPath)
    }

    @Test
    fun `unfollow feed uses delete and accepts no content response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = "",
                status = HttpStatusCode.NoContent,
            )
        }

        UnfollowFeedMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(feedId = "feed-4")

        assertEquals(HttpMethod.Delete, capturedRequest?.method)
        assertEquals("/users/me/feeds/feed-4/follow", capturedRequest?.url?.encodedPath)
    }
}

private fun createTestClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): HttpClient = HttpClient(
    engine = MockEngine(handler),
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

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
