package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.AuthSessionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * Encapsulates the `/auth/sessions` transport call.
 */
internal class GetAuthSessionsMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(): List<AuthSessionDto> = httpClient
        .get {
            url("${config.baseUrl}/auth/sessions")
        }
        .body()
}
