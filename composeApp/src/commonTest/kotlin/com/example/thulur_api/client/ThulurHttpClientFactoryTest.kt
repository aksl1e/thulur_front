package com.example.thulur_api.client

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.time.Duration.Companion.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ThulurHttpClientFactoryTest {
    @Test
    fun `adds bearer header when token is available`() = runTest {
        var authorizationHeader: String? = null
        val client = createThulurHttpClient(
            config = ThulurApiConfig(),
            engine = MockEngine { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                )
            },
            currentTokenProvider = { "token-1" },
        )

        client.get("https://example.com/test")

        assertEquals("Bearer token-1", authorizationHeader)
    }

    @Test
    fun `omits bearer header when token is absent`() = runTest {
        var authorizationHeader: String? = "unexpected"
        val client = createThulurHttpClient(
            config = ThulurApiConfig(),
            engine = MockEngine { request ->
                authorizationHeader = request.headers[HttpHeaders.Authorization]
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                )
            },
            currentTokenProvider = { null },
        )

        client.get("https://example.com/test")

        assertNull(authorizationHeader)
    }

    @Test
    fun `401 response calls unauthorized callback and still throws`() = runTest {
        var unauthorizedCalled = false
        val client = createThulurHttpClient(
            config = ThulurApiConfig(),
            engine = MockEngine {
                respond(
                    content = "",
                    status = HttpStatusCode.Unauthorized,
                )
            },
            onUnauthorized = {
                unauthorizedCalled = true
            },
        )

        assertFailsWith<ClientRequestException> {
            client.get("https://example.com/test")
        }

        assertTrue(unauthorizedCalled)
    }

    @Test
    fun `200 response does not call unauthorized callback`() = runTest {
        var unauthorizedCalled = false
        val client = createThulurHttpClient(
            config = ThulurApiConfig(),
            engine = MockEngine {
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                )
            },
            onUnauthorized = {
                unauthorizedCalled = true
            },
        )

        client.get("https://example.com/test")

        assertFalse(unauthorizedCalled)
    }

    @Test
    fun `403 response does not call unauthorized callback`() = runTest {
        var unauthorizedCalled = false
        val client = createThulurHttpClient(
            config = ThulurApiConfig(),
            engine = MockEngine {
                respond(
                    content = "",
                    status = HttpStatusCode.Forbidden,
                )
            },
            onUnauthorized = {
                unauthorizedCalled = true
            },
        )

        assertFailsWith<ClientRequestException> {
            client.get("https://example.com/test")
        }

        assertFalse(unauthorizedCalled)
    }

    @Test
    fun `uses default timeout for ordinary requests`() = runTest {
        val config = ThulurApiConfig(defaultTimeout = 23.seconds)
        var requestTimeoutMillis: Long? = null
        val client = createThulurHttpClient(
            config = config,
            engine = MockEngine { request ->
                requestTimeoutMillis = request.getCapabilityOrNull(HttpTimeoutCapability)?.requestTimeoutMillis
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                )
            },
        )

        client.get("https://example.com/test")

        assertEquals(config.defaultTimeout.inWholeMilliseconds, requestTimeoutMillis)
    }
}
