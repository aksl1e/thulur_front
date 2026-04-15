package com.example.thulur_api.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun providePlatformThulurHttpClientEngineFactory(): HttpClientEngineFactory<*> = CIO