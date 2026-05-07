package com.example.thulur.data.session

import com.example.thulur.domain.session.CurrentSession
import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.session.ReadArticlesCache
import com.example.thulur.domain.session.SecureTokenStore
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrentSessionProviderImpl(
    private val tokenStore: SecureTokenStore,
    private val readArticlesCache: ReadArticlesCache,
) : CurrentSessionProvider {
    private val _sessionFlow = MutableStateFlow<CurrentSession?>(null)
    private var nextSessionInstanceId = 1

    override val sessionFlow: StateFlow<CurrentSession?> = _sessionFlow.asStateFlow()

    override fun currentSession(): CurrentSession? = _sessionFlow.value

    override fun currentToken(): String? = _sessionFlow.value?.token

    override suspend fun loadPersistedToken() {
        if (_sessionFlow.value != null) return

        ignoreStorageFailure {
            tokenStore.readToken()?.takeIf(String::isNotBlank)
        }?.let { token ->
            _sessionFlow.value = CurrentSession(
                token = token,
                instanceId = nextSessionInstanceId++,
            )
            println(token)
        }
    }

    override suspend fun updateToken(token: String) {
        val normalizedToken = token.takeIf(String::isNotBlank)
        if (normalizedToken == null) {
            clearToken()
            return
        }

        val currentSession = _sessionFlow.value
        if (currentSession == null) {
            readArticlesCache.clear()
        }

        _sessionFlow.value = CurrentSession(
            token = normalizedToken,
            instanceId = currentSession?.instanceId ?: nextSessionInstanceId++,
        )
        ignoreStorageFailure {
            tokenStore.writeToken(normalizedToken)
        }
        println(token)
    }

    override suspend fun clearToken() {
        _sessionFlow.value = null
        readArticlesCache.clear()
        ignoreStorageFailure {
            tokenStore.clearToken()
        }
    }

    private suspend fun <T> ignoreStorageFailure(block: suspend () -> T): T? =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            null
        }
}
