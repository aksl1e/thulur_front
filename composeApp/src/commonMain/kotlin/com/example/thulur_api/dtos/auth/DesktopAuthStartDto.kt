package com.example.thulur_api.dtos.auth

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Browser start payload for the desktop auth flow.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DesktopAuthStartDto(
    @JsonNames("browser_url")
    val browserUrl: String,
)

@Serializable
enum class DesktopAuthMode {
    @SerialName("login")
    Login,

    @SerialName("register")
    Register,
}
