package com.example.thulur.data.session

import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.session.SecureTokenStore
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrentSessionProviderImpl(
    private val tokenStore: SecureTokenStore,
) : CurrentSessionProvider {
    private val _tokenFlow = MutableStateFlow<String?>(null)

    override val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    override fun currentToken(): String? = _tokenFlow.value

    override suspend fun loadPersistedToken() {
        if (!_tokenFlow.value.isNullOrBlank()) return

        _tokenFlow.value = ignoreStorageFailure {
            tokenStore.readToken()?.takeIf(String::isNotBlank)
        }
    }

    override suspend fun updateToken(token: String) {
        val normalizedToken = token.takeIf(String::isNotBlank)
        if (normalizedToken == null) {
            clearToken()
            return
        }

        _tokenFlow.value = normalizedToken
        ignoreStorageFailure {
            tokenStore.writeToken(normalizedToken)
        }
    }

    override suspend fun clearToken() {
        _tokenFlow.value = null
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
