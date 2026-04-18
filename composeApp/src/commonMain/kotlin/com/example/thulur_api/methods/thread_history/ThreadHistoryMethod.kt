package com.example.thulur_api.methods.thread_history

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.ThreadHistoryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me/threads/{thread_id}/history` transport call.
 */
internal class ThreadHistoryMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    /**
     * Requests thread history for the given thread id.
     *
     * @param threadId Backend thread identifier.
     * @return Raw backend response as [ThreadHistoryDto].
     */
    suspend fun execute(
        threadId: String,
    ): ThreadHistoryDto = httpClient
        .get {
            url("${config.baseUrl}/users/me/threads/$threadId/history")
        }
        .body()
}
