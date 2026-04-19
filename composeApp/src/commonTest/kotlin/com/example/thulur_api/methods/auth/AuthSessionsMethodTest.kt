package com.example.thulur_api.methods.auth

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

class AuthSessionsMethodTest {
    @Test
    fun `get auth sessions requests correct path and deserializes payload`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    [
                      {
                        "sessionId": "session-1",
                        "deviceName": "MacBook Pro",
                        "platform": "desktop",
                        "city": "Torun",
                        "country": "Poland",
                        "lastSeenAt": "2026-04-18T09:00:00Z"
                      },
                      {
                        "sessionId": "session-2",
                        "deviceName": "Galaxy S25",
                        "platform": "mobile",
                        "lastSeenAt": "2026-04-17T08:00:00Z"
                      }
                    ]
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = GetAuthSessionsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/auth/sessions", capturedRequest?.url?.encodedPath)
        assertEquals("session-1", response.first().sessionId)
        assertEquals("MacBook Pro", response.first().deviceName)
        assertEquals("desktop", response.first().platform)
        assertNull(response.last().city)
        assertNull(response.last().country)
    }

    @Test
    fun `terminate auth session uses delete and accepts no content response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = "",
                status = HttpStatusCode.NoContent,
            )
        }

        TerminateAuthSessionMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(sessionId = "session-3")

        assertEquals(HttpMethod.Delete, capturedRequest?.method)
        assertEquals("/auth/terminate/session-3", capturedRequest?.url?.encodedPath)
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
