package com.example.thulur_api.methods.feeds

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me/feeds/{feed_id}/follow` follow call.
 */
internal class FollowFeedMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(feedId: String) {
        httpClient.post {
            url("${config.baseUrl}/users/me/feeds/$feedId/follow")
        }
    }
}
