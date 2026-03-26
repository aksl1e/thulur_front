package com.example.thulur_api.client

import io.ktor.client.engine.HttpClientEngineFactory

/**
 * Provides the platform-specific Ktor engine factory used by [createThulurHttpClient].
 */
expect fun providePlatformThulurHttpClientEngineFactory(): HttpClientEngineFactory<*>
