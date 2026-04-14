package com.example.thulur.data.session

import com.example.thulur.domain.session.SecureTokenStore

class InMemorySecureTokenStore(
    initialToken: String? = null,
) : SecureTokenStore {
    private var token: String? = initialToken?.takeIf(String::isNotBlank)

    override suspend fun readToken(): String? = token

    override suspend fun writeToken(token: String) {
        this.token = token.takeIf(String::isNotBlank)
    }

    override suspend fun clearToken() {
        token = null
    }
}
