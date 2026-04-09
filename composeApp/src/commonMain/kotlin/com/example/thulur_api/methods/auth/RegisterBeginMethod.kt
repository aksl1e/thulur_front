package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.auth.RegistrationOptionsDto
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
 * Encapsulates the `/auth/register/begin` transport call.
 */
internal class RegisterBeginMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    suspend fun execute(email: String): RegistrationOptionsDto = httpClient
        .post {
            url("${config.baseUrl}/auth/register/begin")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterBeginRequest(email = email))
        }
        .body()
}

@Serializable
private data class RegisterBeginRequest(
    @SerialName("email")
    val email: String,
)
