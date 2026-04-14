package com.example.thulur.domain.session

interface SecureTokenStore {
    suspend fun readToken(): String?

    suspend fun writeToken(token: String)

    suspend fun clearToken()
}

expect fun providePlatformSecureTokenStore(): SecureTokenStore
