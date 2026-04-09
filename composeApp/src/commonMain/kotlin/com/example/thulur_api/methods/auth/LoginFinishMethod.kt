package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.auth.AuthStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Encapsulates the `/auth/login/finish` transport call.
 */
internal class LoginFinishMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(
        email: String,
        credential: JsonObject,
    ): AuthStatusDto = httpClient
        .post {
            url("${config.baseUrl}/auth/login/finish")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                LoginFinishRequest(
                    email = email,
                    credential = credential,
                ),
            )
        }
        .body()
}

@Serializable
private data class LoginFinishRequest(
    @SerialName("email")
    val email: String,
    @SerialName("credential")
    val credential: JsonObject,
)
