package com.example.thulur_api.methods.chat

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.chat.ChatRequestDto
import com.example.thulur_api.dtos.chat.ChatResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Encapsulates the `POST /users/me/threads/{thread_id}/chat` transport call.
 */
internal class ThreadChatMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(
        threadId: String,
        message: String,
    ): ChatResponseDto = httpClient
        .post {
            url("${config.baseUrl}/users/me/threads/$threadId/chat")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(ChatRequestDto(message = message))
        }
        .body()
}
