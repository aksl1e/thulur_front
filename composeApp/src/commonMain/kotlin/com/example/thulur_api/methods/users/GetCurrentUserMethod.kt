package com.example.thulur_api.methods.users

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me` transport call.
 */
internal class GetCurrentUserMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(): UserDto = httpClient
        .get {
            url("${config.baseUrl}/users/me")
        }
        .body()
}
