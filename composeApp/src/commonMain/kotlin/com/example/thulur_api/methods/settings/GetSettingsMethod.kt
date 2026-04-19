package com.example.thulur_api.methods.settings

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.UserSettingsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me/settings` read transport call.
 */
internal class GetSettingsMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(): UserSettingsDto = httpClient
        .get {
            url("${config.baseUrl}/users/me/settings")
        }
        .body()
}
