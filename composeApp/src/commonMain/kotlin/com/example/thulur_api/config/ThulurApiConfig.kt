package com.example.thulur_api.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Runtime configuration for the Thulur API transport layer.
 */
data class ThulurApiConfig(
    val baseUrl: String = "https://api.thulur.com",
    val defaultTimeout: Duration = 15.seconds,
    val chatTimeout: Duration = 60.seconds,
)
