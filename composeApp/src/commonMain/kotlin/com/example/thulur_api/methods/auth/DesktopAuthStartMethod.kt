package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.auth.DesktopAuthMode
import com.example.thulur_api.dtos.auth.DesktopAuthStartDto
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
 * Starts a backend-hosted desktop auth flow and returns an opaque browser URL.
 */
internal class DesktopAuthStartMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto = httpClient
        .post {
            url("${config.baseUrl}/auth/desktop/start")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                DesktopAuthStartRequest(
                    email = email,
                    mode = mode,
                    callbackUrl = callbackUrl,
                    state = state,
                ),
            )
        }
        .body()
}

@Serializable
private data class DesktopAuthStartRequest(
    @SerialName("email")
    val email: String,
    @SerialName("mode")
    val mode: DesktopAuthMode,
    @SerialName("callbackUrl")
    val callbackUrl: String,
    @SerialName("state")
    val state: String,
)
