package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.auth.AuthTokenDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Exchanges the one-time desktop auth callback code for an app bearer token.
 */
internal class DesktopAuthExchangeMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(
        code: String,
        state: String,
    ): AuthTokenDto = httpClient
        .post {
            url("${config.baseUrl}/auth/exchange")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                CodeExchangeRequest(
                    code = code,
                    state = state,
                ),
            )
        }
        .body()
}

@Serializable
private data class CodeExchangeRequest(
    @SerialName("code")
    val code: String,
    @SerialName("state")
    val state: String,
)
