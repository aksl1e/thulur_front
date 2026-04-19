package com.example.thulur_api.methods.settings

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.UpdateUserSettingsDto
import com.example.thulur_api.dtos.UserSettingsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Encapsulates the `/users/me/settings` update transport call.
 */
internal class PatchSettingsMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(update: UpdateUserSettingsDto): UserSettingsDto = httpClient
        .patch {
            url("${config.baseUrl}/users/me/settings")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(update)
        }
        .body()
}
