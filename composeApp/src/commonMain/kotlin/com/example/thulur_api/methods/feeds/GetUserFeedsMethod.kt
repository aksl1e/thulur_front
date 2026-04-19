package com.example.thulur_api.methods.feeds

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.FeedDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me/feeds` transport call.
 */
internal class GetUserFeedsMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(): List<FeedDto> = httpClient
        .get {
            url("${config.baseUrl}/users/me/feeds")
        }
        .body()
}
