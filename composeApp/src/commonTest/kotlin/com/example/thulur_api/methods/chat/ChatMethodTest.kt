package com.example.thulur_api.methods.chat

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

class ChatMethodTest {
    @Test
    fun `general chat uses post json body and deserializes response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "response": "Feed reply" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = GeneralChatMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(message = "Hello feed")

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/users/me/chat", capturedRequest?.url?.encodedPath)
        assertEquals(ContentType.Application.Json, requestBodyContentType(capturedRequest))
        assertEquals(
            buildJsonObject {
                put("message", "Hello feed")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("Feed reply", response.response)
    }

    @Test
    fun `thread chat uses thread path json body and deserializes response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "response": "Thread reply" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = ThreadChatMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            threadId = "thread-1",
            message = "Hello thread",
        )

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/users/me/threads/thread-1/chat", capturedRequest?.url?.encodedPath)
        assertEquals(ContentType.Application.Json, requestBodyContentType(capturedRequest))
        assertEquals(
            buildJsonObject {
                put("message", "Hello thread")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("Thread reply", response.response)
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

private fun requestBodyContentType(request: HttpRequestData?): ContentType? =
    checkNotNull(request).body.contentType

private val testJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
