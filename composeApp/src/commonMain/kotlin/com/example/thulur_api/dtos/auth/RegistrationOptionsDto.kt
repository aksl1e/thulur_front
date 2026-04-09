package com.example.thulur_api.dtos.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw WebAuthn registration options returned by `/auth/register/begin`.
 */
@Serializable
data class RegistrationOptionsDto(
    @SerialName("rp")
    val rp: AuthRelyingPartyDto,
    @SerialName("user")
    val user: AuthUserDto,
    @SerialName("challenge")
    val challenge: String,
    @SerialName("pubKeyCredParams")
    val pubKeyCredParams: List<AuthPubKeyCredParamDto>,
    @SerialName("timeout")
    val timeout: Long,
    @SerialName("attestation")
    val attestation: String,
    @SerialName("authenticatorSelection")
    val authenticatorSelection: AuthenticatorSelectionDto? = null,
    @SerialName("excludeCredentials")
    val excludeCredentials: List<AuthCredentialDescriptorDto> = emptyList(),
)
