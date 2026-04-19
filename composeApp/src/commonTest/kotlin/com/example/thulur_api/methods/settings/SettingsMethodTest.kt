package com.example.thulur_api.methods.settings

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.UpdateUserSettingsDto
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

class SettingsMethodTest {
    @Test
    fun `get settings requests correct path and deserializes response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    {
                      "user_id": "user-1",
                      "dark_mode": true,
                      "suggestions_outside": false,
                      "min_quality_score": 0.65,
                      "language": "pl",
                      "notifications_enabled": true,
                      "notifications_time": "08:30:00",
                      "timezone": "Europe/Warsaw",
                      "updated_at": "2026-04-18T09:15:00"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = GetSettingsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute()

        assertEquals(HttpMethod.Get, capturedRequest?.method)
        assertEquals("/users/me/settings", capturedRequest?.url?.encodedPath)
        assertEquals("user-1", response.userId)
        assertEquals(true, response.darkMode)
        assertEquals(false, response.suggestionsOutside)
        assertEquals(0.65, response.minQualityScore)
        assertEquals("pl", response.language)
        assertEquals(true, response.notificationsEnabled)
        assertEquals("08:30:00", response.notificationsTime)
        assertEquals("Europe/Warsaw", response.timezone)
        assertEquals("2026-04-18T09:15:00", response.updatedAt)
    }

    @Test
    fun `patch settings sends only non null fields and returns updated response`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    {
                      "user_id": "user-1",
                      "dark_mode": true,
                      "suggestions_outside": true,
                      "min_quality_score": 0.7,
                      "language": "en",
                      "notifications_enabled": false,
                      "notifications_time": "09:00:00",
                      "timezone": "Europe/Warsaw",
                      "updated_at": "2026-04-18T09:30:00"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = PatchSettingsMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            update = UpdateUserSettingsDto(
                darkMode = true,
                minQualityScore = 0.7,
                notificationsTime = "09:00:00",
                timezone = "Europe/Warsaw",
            ),
        )

        assertEquals(HttpMethod.Patch, capturedRequest?.method)
        assertEquals("/users/me/settings", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("dark_mode", true)
                put("min_quality_score", 0.7)
                put("notifications_time", "09:00:00")
                put("timezone", "Europe/Warsaw")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals(true, response.darkMode)
        assertEquals("09:00:00", response.notificationsTime)
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
