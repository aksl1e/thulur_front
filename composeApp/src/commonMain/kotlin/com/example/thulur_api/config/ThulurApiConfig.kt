package com.example.thulur_api.config

/**
 * Runtime configuration for the Thulur API transport layer.
 */
data class ThulurApiConfig(
    /**
     * Base URL of the Thulur backend.
     *
     * TODO: update API base URL/configuration when environment setup is finalized.
     */
    val baseUrl: String = "http://localhost:8002",
)
