package com.example.thulur.domain.auth

import com.example.thulur_api.ThulurApi

interface PasskeyAuthenticator {
    suspend fun login(email: String): String

    suspend fun register(email: String): String
}

class PasskeyAuthenticationException(
    message: String,
    val code: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

object PasskeyAuthenticationErrorCode {
    const val UserNotFound: String = "USER_NOT_FOUND"
    const val PasskeyCancelled: String = "PASSKEY_CANCELLED"
    const val PasskeyNotSupported: String = "PASSKEY_NOT_SUPPORTED"
    const val AuthFailed: String = "AUTH_FAILED"
    const val InvalidState: String = "INVALID_STATE"
    const val MissingCode: String = "MISSING_CODE"
}

expect fun providePlatformPasskeyAuthenticator(thulurApi: ThulurApi): PasskeyAuthenticator
