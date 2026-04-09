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
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class AuthMethodTest {
    @Test
    fun `register begin sends email and parses full registration options`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    {
                      "rp": { "id": "localhost", "name": "Thulur" },
                      "user": {
                        "id": "dXNlci1pZA",
                        "name": "hello@example.com",
                        "displayName": "Hello"
                      },
                      "challenge": "challenge-value",
                      "pubKeyCredParams": [
                        { "type": "public-key", "alg": -7 },
                        { "type": "public-key", "alg": -257 }
                      ],
                      "timeout": 60000,
                      "attestation": "none",
                      "authenticatorSelection": {
                        "authenticatorAttachment": "platform",
                        "residentKey": "preferred",
                        "requireResidentKey": false,
                        "userVerification": "preferred"
                      },
                      "excludeCredentials": [
                        { "type": "public-key", "id": "cred-1" }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = RegisterBeginMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(email = "hello@example.com")

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/register/begin", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("email", "hello@example.com")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("localhost", response.rp.id)
        assertEquals("Hello", response.user.displayName)
        assertEquals(2, response.pubKeyCredParams.size)
        assertEquals("platform", response.authenticatorSelection?.authenticatorAttachment)
        assertEquals(listOf("cred-1"), response.excludeCredentials.map { it.id })
    }

    @Test
    fun `register begin tolerates missing optional fields`() = runTest {
        val client = createTestClient {
            respond(
                content = """
                    {
                      "rp": { "id": "localhost", "name": "Thulur" },
                      "user": {
                        "id": "dXNlci1pZA",
                        "name": "hello@example.com",
                        "displayName": "Hello"
                      },
                      "challenge": "challenge-value",
                      "pubKeyCredParams": [
                        { "type": "public-key", "alg": -7 }
                      ],
                      "timeout": 60000,
                      "attestation": "none"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = RegisterBeginMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(email = "hello@example.com")

        assertNull(response.authenticatorSelection)
        assertEquals(emptyList(), response.excludeCredentials)
    }

    @Test
    fun `login begin sends email and defaults allow credentials to empty`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """
                    {
                      "challenge": "challenge-value",
                      "timeout": 30000,
                      "rpId": "localhost",
                      "userVerification": "preferred"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = LoginBeginMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(email = "hello@example.com")

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/login/begin", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("email", "hello@example.com")
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals(emptyList(), response.allowCredentials)
        assertEquals("preferred", response.userVerification)
    }

    @Test
    fun `login begin parses allow credentials when present`() = runTest {
        val client = createTestClient {
            respond(
                content = """
                    {
                      "challenge": "challenge-value",
                      "timeout": 30000,
                      "rpId": "localhost",
                      "userVerification": null,
                      "allowCredentials": [
                        { "type": "public-key", "id": "cred-1" },
                        { "type": "public-key", "id": "cred-2" }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }

        val response = LoginBeginMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(email = "hello@example.com")

        assertNull(response.userVerification)
        assertEquals(listOf("cred-1", "cred-2"), response.allowCredentials.map { it.id })
    }

    @Test
    fun `register finish sends credential payload unchanged and parses status`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "status": "ok" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val credential = registrationCredential()

        val response = RegisterFinishMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            email = "hello@example.com",
            credential = credential,
        )

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/register/finish", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("email", "hello@example.com")
                put("credential", credential)
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("ok", response.status)
    }

    @Test
    fun `login finish sends credential payload unchanged and parses status`() = runTest {
        var capturedRequest: HttpRequestData? = null
        val client = createTestClient {
            capturedRequest = it
            respond(
                content = """{ "status": "ok" }""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val credential = loginCredential()

        val response = LoginFinishMethod(
            httpClient = client,
            config = ThulurApiConfig(),
        ).execute(
            email = "hello@example.com",
            credential = credential,
        )

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("/auth/login/finish", capturedRequest?.url?.encodedPath)
        assertEquals(
            buildJsonObject {
                put("email", "hello@example.com")
                put("credential", credential)
            },
            requestBodyAsJsonObject(capturedRequest),
        )
        assertEquals("ok", response.status)
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

private fun registrationCredential(): JsonObject = buildJsonObject {
    put("id", "credential-id")
    put("rawId", "credential-raw-id")
    put("type", "public-key")
    putJsonObject("response") {
        put("clientDataJSON", "client-data-json")
        put("attestationObject", "attestation-object")
    }
}

private fun loginCredential(): JsonObject = buildJsonObject {
    put("id", "credential-id")
    put("rawId", "credential-raw-id")
    put("type", "public-key")
    putJsonObject("response") {
        put("clientDataJSON", "client-data-json")
        put("authenticatorData", "authenticator-data")
        put("signature", "signature")
    }
}

private val testJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
