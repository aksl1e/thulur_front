package com.example.thulur_api.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates a Ktor [HttpClient] configured for the Thulur backend.
 *
 * The client ignores unknown keys on purpose so the app can keep working
 */
fun createThulurHttpClient(
    engineFactory: HttpClientEngineFactory<*> = providePlatformThulurHttpClientEngineFactory(),
    currentTokenProvider: () -> String? = { null },
    onUnauthorized: suspend () -> Unit = {},
): HttpClient = HttpClient(engineFactory) {
    configureThulurHttpClient(
        currentTokenProvider = currentTokenProvider,
        onUnauthorized = onUnauthorized,
    )
}

internal fun createThulurHttpClient(
    engine: HttpClientEngine,
    currentTokenProvider: () -> String? = { null },
    onUnauthorized: suspend () -> Unit = {},
): HttpClient = HttpClient(engine) {
    configureThulurHttpClient(
        currentTokenProvider = currentTokenProvider,
        onUnauthorized = onUnauthorized,
    )
}

private fun HttpClientConfig<*>.configureThulurHttpClient(
    currentTokenProvider: () -> String?,
    onUnauthorized: suspend () -> Unit,
) {
    expectSuccess = true

    defaultRequest {
        currentTokenProvider()
            ?.takeIf(String::isNotBlank)
            ?.let { token -> header(HttpHeaders.Authorization, "Bearer $token") }
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val requestException = cause as? ClientRequestException ?: return@handleResponseExceptionWithRequest
            if (requestException.response.status == HttpStatusCode.Unauthorized) {
                onUnauthorized()
            }
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }
}
