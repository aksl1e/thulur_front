package com.example.thulur_api.dtos.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthCredentialDescriptorDto(
    @SerialName("type")
    val type: String,
    @SerialName("id")
    val id: String,
)

@Serializable
data class AuthRelyingPartyDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
)

@Serializable
data class AuthUserDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("displayName")
    val displayName: String,
)

@Serializable
data class AuthPubKeyCredParamDto(
    @SerialName("type")
    val type: String,
    @SerialName("alg")
    val alg: Int,
)

@Serializable
data class AuthenticatorSelectionDto(
    @SerialName("authenticatorAttachment")
    val authenticatorAttachment: String? = null,
    @SerialName("residentKey")
    val residentKey: String? = null,
    @SerialName("requireResidentKey")
    val requireResidentKey: Boolean? = null,
    @SerialName("userVerification")
    val userVerification: String? = null,
)
