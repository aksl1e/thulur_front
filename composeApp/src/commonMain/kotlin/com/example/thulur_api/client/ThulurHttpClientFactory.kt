package com.example.thulur_api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates a Ktor [HttpClient] configured for the Thulur backend.
 *
 * The client ignores unknown keys on purpose so the app can keep working
 */
fun createThulurHttpClient(
    engineFactory: HttpClientEngineFactory<*> = providePlatformThulurHttpClientEngineFactory(),
): HttpClient = HttpClient(engineFactory) {
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
