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
import io.ktor.http.Url
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
    fun `login page URL contains encoded desktop auth query`() {
        val url = Url(
            DesktopLoginPageUrlMethod(ThulurApiConfig()).execute(
                email = "hello+test@example.com",
                callbackUrl = "http://127.0.0.1:54321/auth/callback",
                state = "state value",
            ),
        )

        assertEquals("/auth/login", url.encodedPath)
        assertEquals("hello+test@example.com", url.parameters["email"])
        assertEquals("http://127.0.0.1:54321/auth/callback", url.parameters["callback_url"])
        assertEquals("state value", url.parameters["state"])
    }

    @Test
    fun `registration page URL contains encoded desktop auth query`() {
        val url = Url(
            DesktopRegistrationPageUrlMethod(ThulurApiConfig()).execute(
                email = "hello+test@example.com",
                callbackUrl = "http://127.0.0.1:54321/auth/callback",
                state = "state value",
            ),
        )

        assertEquals("/auth/register", url.encodedPath)
        assertEquals("hello+test@example.com", url.parameters["email"])
        assertEquals("http://127.0.0.1:54321/auth/callback", url.parameters["callback_url"])
        assertEquals("state value", url.parameters["state"])
    }

    @Test
    fun `exchange sends code and state and parses token`() = runTest {
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
        )

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/exchange", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("code", "code-1")
                put("state", "state-1")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("token-1", response.token)
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
