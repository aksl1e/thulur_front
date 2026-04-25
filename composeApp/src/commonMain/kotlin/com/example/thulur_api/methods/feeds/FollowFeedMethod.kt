package com.example.thulur_api.methods.feeds

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.feeds.FollowFeedRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Encapsulates the `/users/me/feeds/follow` follow call.
 */
internal class FollowFeedMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(identifier: String) {
        httpClient.post {
            url("${config.baseUrl}/users/me/feeds/follow")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(FollowFeedRequest(identifier = identifier))
        }
    }
}
