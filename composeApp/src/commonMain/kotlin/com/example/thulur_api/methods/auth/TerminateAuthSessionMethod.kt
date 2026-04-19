package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.url

/**
 * Encapsulates the `/auth/terminate/{session_id}` transport call.
 */
internal class TerminateAuthSessionMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(sessionId: String) {
        httpClient.delete {
            url("${config.baseUrl}/auth/terminate/$sessionId")
        }
    }
}
