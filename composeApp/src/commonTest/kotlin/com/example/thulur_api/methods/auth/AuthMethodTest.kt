package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.auth.DesktopAuthMode
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
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class AuthMethodTest {
    @Test
    fun `desktop auth start sends body and parses browser url`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "browserUrl": "http://localhost:8002/auth/flow/flow-1" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = DesktopAuthStartMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            email = "hello+test@example.com",
            mode = DesktopAuthMode.Login,
            callbackUrl = "http://127.0.0.1:54321/auth/callback",
            state = "state value",
        )

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/desktop/start", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("email", "hello+test@example.com")
                put("mode", "login")
                put("callbackUrl", "http://127.0.0.1:54321/auth/callback")
                put("state", "state value")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("http://localhost:8002/auth/flow/flow-1", response.browserUrl)
    }

    @Test
    fun `desktop auth start accepts snake case browser url`() = runTest {
        val client = createTestClient {
            respond(
                content = """{ "browser_url": "http://localhost:8002/auth/flow/flow-2" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = DesktopAuthStartMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            email = "hello@example.com",
            mode = DesktopAuthMode.Register,
            callbackUrl = "http://127.0.0.1:54321/auth/callback",
            state = "state-2",
        )

        assertEquals("http://localhost:8002/auth/flow/flow-2", response.browserUrl)
    }

    @Test
    fun `exchange sends code state device and platform and parses token`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "token": "token-1" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = DesktopAuthExchangeMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            code = "code-1",
            state = "state-1",
            deviceName = "MacBook Pro",
            platform = "macos",
        )

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/exchange", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("code", "code-1")
                put("state", "state-1")
                put("device_name", "MacBook Pro")
                put("platform", "macos")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("token-1", response.token)
    }

    @Test
    fun `exchange omits device and platform when null`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "token": "token-2" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        DesktopAuthExchangeMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            code = "code-2",
            state = "state-2",
        )

        assertEquals(
            buildJsonObject {
                put("code", "code-2")
                put("state", "state-2")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
    }
}

private fun createTestClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): HttpClient = HttpClient(
    engine = MockEngine(handler),
) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(testJson)
    }
}

private fun requestBodyAsJsonObject(request: HttpRequestData?): JsonObject {
    val rawBody = checkNotNull(request).body
    val text = when (rawBody) {
        is TextContent -> rawBody.text
        is OutgoingContent.ByteArrayContent -> rawBody.bytes().decodeToString()
        else -> error("Unexpected request body type: ${rawBody::class.simpleName}")
    }

    return testJson.parseToJsonElement(text).jsonObject
}

private val testJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
