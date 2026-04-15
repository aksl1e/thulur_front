package com.example.thulur.domain.session

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides the temporary app session used by authenticated transport calls.
 */
data class CurrentSession(
    val token: String,
    val instanceId: Int,
)

interface CurrentSessionProvider {
    val sessionFlow: StateFlow<CurrentSession?>

    fun currentSession(): CurrentSession?

    fun currentToken(): String?

    suspend fun loadPersistedToken()

    suspend fun updateToken(token: String)

    suspend fun clearToken()
}
