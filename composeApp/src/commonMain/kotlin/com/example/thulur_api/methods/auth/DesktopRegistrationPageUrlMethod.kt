package com.example.thulur_api.methods.auth

import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.http.URLBuilder

/**
 * Builds the backend-hosted WebAuthn registration page URL used by desktop auth.
 */
internal class DesktopRegistrationPageUrlMethod(
    private val config: ThulurApiConfig,
) {
    fun execute(
        email: String,
        callbackUrl: String,
        state: String,
    ): String = URLBuilder("${config.baseUrl}/auth/register").apply {
        parameters.append("email", email)
        parameters.append("callback_url", callbackUrl)
        parameters.append("state", state)
    }.buildString()
}
