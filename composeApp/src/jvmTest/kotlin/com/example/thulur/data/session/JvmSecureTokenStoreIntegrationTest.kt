package com.example.thulur.data.session

import com.example.thulur.domain.session.providePlatformSecureTokenStore
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class JvmSecureTokenStoreIntegrationTest {
    @Test
    fun `platform secure token store round trips token when explicitly enabled`() = runTest {
        if (System.getenv("THULUR_TEST_PLATFORM_SECURE_TOKEN_STORE") != "true") {
            return@runTest
        }

        val tokenStore = providePlatformSecureTokenStore()
        if (tokenStore is InMemorySecureTokenStore) {
            return@runTest
        }

        val token = "test-token-${UUID.randomUUID()}"
        try {
            tokenStore.writeToken(token)

            assertEquals(token, tokenStore.readToken())
        } finally {
            tokenStore.clearToken()
        }

        assertNull(tokenStore.readToken())
    }
}
