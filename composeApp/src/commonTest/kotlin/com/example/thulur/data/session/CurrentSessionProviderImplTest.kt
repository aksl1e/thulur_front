package com.example.thulur.data.session

import com.example.thulur.domain.session.CurrentSession
import com.example.thulur.domain.session.SecureTokenStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class CurrentSessionProviderImplTest {
    @Test
    fun `starts with no runtime token before load`() {
        val provider = CurrentSessionProviderImpl(InMemorySecureTokenStore(initialToken = "token-1"))

        assertNull(provider.currentToken())
        assertNull(provider.currentSession())
        assertNull(provider.sessionFlow.value)
    }

    @Test
    fun `load persisted token emits stored token`() = runTest {
        val provider = CurrentSessionProviderImpl(InMemorySecureTokenStore(initialToken = "token-1"))

        provider.loadPersistedToken()

        assertEquals("token-1", provider.currentToken())
        assertEquals(
            CurrentSession(token = "token-1", instanceId = 1),
            provider.currentSession(),
        )
        assertEquals(
            CurrentSession(token = "token-1", instanceId = 1),
            provider.sessionFlow.value,
        )
    }

    @Test
    fun `update token writes to store and emits runtime token`() = runTest {
        val tokenStore = InMemorySecureTokenStore()
        val provider = CurrentSessionProviderImpl(tokenStore)

        provider.updateToken("token-1")

        assertEquals("token-1", provider.currentToken())
        assertEquals("token-1", tokenStore.readToken())
    }

    @Test
    fun `clear token resets runtime token and store`() = runTest {
        val tokenStore = InMemorySecureTokenStore(initialToken = "token-1")
        val provider = CurrentSessionProviderImpl(tokenStore)
        provider.loadPersistedToken()

        provider.clearToken()

        assertNull(provider.currentToken())
        assertNull(provider.currentSession())
        assertNull(tokenStore.readToken())
    }

    @Test
    fun `storage read failure does not crash and leaves token empty`() = runTest {
        val provider = CurrentSessionProviderImpl(FailingSecureTokenStore(failRead = true))

        provider.loadPersistedToken()

        assertNull(provider.currentToken())
        assertNull(provider.currentSession())
        assertNull(provider.sessionFlow.value)
    }

    @Test
    fun `storage write failure keeps runtime token`() = runTest {
        val provider = CurrentSessionProviderImpl(FailingSecureTokenStore(failWrite = true))

        provider.updateToken("token-1")

        assertEquals("token-1", provider.currentToken())
        assertEquals(
            CurrentSession(token = "token-1", instanceId = 1),
            provider.currentSession(),
        )
    }

    @Test
    fun `load does not overwrite existing runtime token`() = runTest {
        val provider = CurrentSessionProviderImpl(InMemorySecureTokenStore())
        provider.updateToken("runtime-token")

        provider.loadPersistedToken()

        assertEquals("runtime-token", provider.currentToken())
        assertEquals(
            CurrentSession(token = "runtime-token", instanceId = 1),
            provider.currentSession(),
        )
    }

    @Test
    fun `new session gets a new instance id after clear`() = runTest {
        val provider = CurrentSessionProviderImpl(InMemorySecureTokenStore())

        provider.updateToken("token-1")
        assertEquals(
            CurrentSession(token = "token-1", instanceId = 1),
            provider.currentSession(),
        )

        provider.clearToken()
        provider.updateToken("token-2")

        assertEquals(
            CurrentSession(token = "token-2", instanceId = 2),
            provider.currentSession(),
        )
    }

    @Test
    fun `token replacement inside active session keeps same instance id`() = runTest {
        val provider = CurrentSessionProviderImpl(InMemorySecureTokenStore())

        provider.updateToken("token-1")
        provider.updateToken("token-2")

        assertEquals(
            CurrentSession(token = "token-2", instanceId = 1),
            provider.currentSession(),
        )
    }
}

private class FailingSecureTokenStore(
    private val failRead: Boolean = false,
    private val failWrite: Boolean = false,
) : SecureTokenStore {
    override suspend fun readToken(): String? {
        if (failRead) error("Read failed")
        return null
    }

    override suspend fun writeToken(token: String) {
        if (failWrite) error("Write failed")
    }

    override suspend fun clearToken() = Unit
}
