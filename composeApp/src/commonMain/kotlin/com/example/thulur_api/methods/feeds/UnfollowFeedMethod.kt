package com.example.thulur_api.methods.feeds

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me/feeds/{feed_id}/follow` unfollow call.
 */
internal class UnfollowFeedMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(feedId: String) {
        httpClient.delete {
            url("${config.baseUrl}/users/me/feeds/$feedId/follow")
        }
    }
}
