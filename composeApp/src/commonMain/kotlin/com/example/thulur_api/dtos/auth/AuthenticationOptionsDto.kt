package com.example.thulur_api.dtos.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw WebAuthn login options returned by `/auth/login/begin`.
 */
@Serializable
data class AuthenticationOptionsDto(
    @SerialName("challenge")
    val challenge: String,
    @SerialName("timeout")
    val timeout: Long,
    @SerialName("rpId")
    val rpId: String,
    @SerialName("userVerification")
    val userVerification: String? = null,
    @SerialName("allowCredentials")
    val allowCredentials: List<AuthCredentialDescriptorDto> = emptyList(),
)
