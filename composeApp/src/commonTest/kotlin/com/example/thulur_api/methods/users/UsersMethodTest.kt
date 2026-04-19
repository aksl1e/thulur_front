package com.example.thulur_api.methods.users

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

class UsersMethodTest {
    @Test
    fun `get current user requests correct path and deserializes response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    {
                      "id": "user-1",
                      "email": "hello@example.com",
                      "subscription_tier": "pro",
                      "subscription_expires_at": null,
                      "created_at": "2026-04-18T09:00:00Z"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = GetCurrentUserMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/users/me", capturedRequest?.url?.encodedPath)
        assertEquals("user-1", response.id)
        assertEquals("hello@example.com", response.email)
        assertEquals("pro", response.subscriptionTier)
        assertNull(response.subscriptionExpiresAt)
        assertEquals("2026-04-18T09:00:00Z", response.createdAt)
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
