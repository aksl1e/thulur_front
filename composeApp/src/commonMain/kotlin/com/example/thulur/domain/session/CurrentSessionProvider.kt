package com.example.thulur.domain.session

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides the temporary app session used by authenticated transport calls.
 */
interface CurrentSessionProvider {
    val tokenFlow: StateFlow<String?>

    fun currentToken(): String?

    suspend fun loadPersistedToken()

    suspend fun updateToken(token: String)

    suspend fun clearToken()
}
