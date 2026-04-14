package com.example.thulur.data.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class InMemorySecureTokenStoreTest {
    @Test
    fun `reads saved token`() = runTest {
        val tokenStore = InMemorySecureTokenStore()

        tokenStore.writeToken("token-1")

        assertEquals("token-1", tokenStore.readToken())
    }

    @Test
    fun `clear removes token`() = runTest {
        val tokenStore = InMemorySecureTokenStore(initialToken = "token-1")

        tokenStore.clearToken()

        assertNull(tokenStore.readToken())
    }

    @Test
    fun `blank token is not persisted`() = runTest {
        val tokenStore = InMemorySecureTokenStore(initialToken = "token-1")

        tokenStore.writeToken(" ")

        assertNull(tokenStore.readToken())
    }
}
