package com.example.thulur.data.session

import com.example.thulur.domain.session.SecureTokenStore
import com.microsoft.credentialstorage.SecretStore
import com.microsoft.credentialstorage.StorageProvider
import com.microsoft.credentialstorage.model.StoredToken
import com.microsoft.credentialstorage.model.StoredTokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class JvmSecureTokenStore private constructor(
    private val tokenStorage: SecretStore<StoredToken>,
) : SecureTokenStore {
    override suspend fun readToken(): String? = withContext(Dispatchers.IO) {
        val storedToken = tokenStorage.get(TOKEN_STORAGE_KEY) ?: return@withContext null
        val tokenChars = storedToken.value

        try {
            String(tokenChars).takeIf(String::isNotBlank)
        } finally {
            tokenChars.fill('\u0000')
            storedToken.clear()
        }
    }

    override suspend fun writeToken(token: String) {
        withContext(Dispatchers.IO) {
            val normalizedToken = token.takeIf(String::isNotBlank)
            if (normalizedToken == null) {
                tokenStorage.delete(TOKEN_STORAGE_KEY)
                return@withContext
            }

            val storedToken = StoredToken(normalizedToken.toCharArray(), StoredTokenType.PERSONAL)
            try {
                tokenStorage.add(TOKEN_STORAGE_KEY, storedToken)
            } finally {
                storedToken.clear()
            }
        }
    }

    override suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            tokenStorage.delete(TOKEN_STORAGE_KEY)
        }
    }

    companion object {
        private const val TOKEN_STORAGE_KEY = "com.example.thulur.auth.token/default"

        fun createOrNull(): JvmSecureTokenStore? {
            val tokenStorage = StorageProvider.getTokenStorage(
                true,
                StorageProvider.SecureOption.REQUIRED,
            ) ?: return null

            if (!tokenStorage.isSecure) return null

            return JvmSecureTokenStore(tokenStorage)
        }
    }
}
