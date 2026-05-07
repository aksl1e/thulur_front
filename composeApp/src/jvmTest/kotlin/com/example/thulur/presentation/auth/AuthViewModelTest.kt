package com.example.thulur.presentation.auth

import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.data.session.InMemoryReadArticlesCache
import com.example.thulur.data.session.InMemorySecureTokenStore
import com.example.thulur.domain.auth.PasskeyAuthenticationErrorCode
import com.example.thulur.domain.auth.PasskeyAuthenticationException
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur.domain.session.SecureTokenStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank email does not start auth flow`() = runTest {
        val authenticator = FakePasskeyAuthenticator()
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val viewModel = AuthViewModel(
            passkeyAuthenticator = authenticator,
            currentSessionProvider = sessionProvider,
        )

        viewModel.onContinueClick()
        advanceUntilIdle()

        assertEquals("Enter your email to continue.", viewModel.uiState.value.errorMessage)
        assertEquals(0, authenticator.loginCalls)
        assertNull(sessionProvider.currentToken())
    }

    @Test
    fun `successful login stores token`() = runTest {
        val authenticator = FakePasskeyAuthenticator(loginResult = Result.success("login-token"))
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val viewModel = AuthViewModel(
            passkeyAuthenticator = authenticator,
            currentSessionProvider = sessionProvider,
        )

        viewModel.onEmailChange(" hello@example.com ")
        viewModel.onContinueClick()
        advanceUntilIdle()

        assertEquals("hello@example.com", authenticator.loginEmail)
        assertEquals(0, authenticator.registerCalls)
        assertEquals("login-token", sessionProvider.currentToken())
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `user not found starts registration fallback and stores register token`() = runTest {
        val authenticator = FakePasskeyAuthenticator(
            loginResult = Result.failure(
                PasskeyAuthenticationException(
                    message = "No passkeys registered for this email.",
                    code = PasskeyAuthenticationErrorCode.UserNotFound,
                ),
            ),
            registerResult = Result.success("register-token"),
        )
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val viewModel = AuthViewModel(
            passkeyAuthenticator = authenticator,
            currentSessionProvider = sessionProvider,
        )

        viewModel.onEmailChange("hello@example.com")
        viewModel.onContinueClick()
        advanceUntilIdle()

        assertEquals("hello@example.com", authenticator.loginEmail)
        assertEquals("hello@example.com", authenticator.registerEmail)
        assertEquals("register-token", sessionProvider.currentToken())
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `passkey cancellation does not register fallback or store token`() = runTest {
        val authenticator = FakePasskeyAuthenticator(
            loginResult = Result.failure(
                PasskeyAuthenticationException(
                    message = "Passkey sign-in was cancelled.",
                    code = PasskeyAuthenticationErrorCode.PasskeyCancelled,
                ),
            ),
        )
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val viewModel = AuthViewModel(
            passkeyAuthenticator = authenticator,
            currentSessionProvider = sessionProvider,
        )

        viewModel.onEmailChange("hello@example.com")
        viewModel.onContinueClick()
        advanceUntilIdle()

        assertEquals("Passkey sign-in was cancelled.", viewModel.uiState.value.errorMessage)
        assertEquals(0, authenticator.registerCalls)
        assertNull(sessionProvider.currentToken())
    }

    @Test
    fun `register fallback failure does not store token`() = runTest {
        val authenticator = FakePasskeyAuthenticator(
            loginResult = Result.failure(
                PasskeyAuthenticationException(
                    message = "No passkeys registered for this email.",
                    code = PasskeyAuthenticationErrorCode.UserNotFound,
                ),
            ),
            registerResult = Result.failure(IllegalStateException("Registration failed")),
        )
        val sessionProvider = createSessionProvider(InMemorySecureTokenStore())
        val viewModel = AuthViewModel(
            passkeyAuthenticator = authenticator,
            currentSessionProvider = sessionProvider,
        )

        viewModel.onEmailChange("hello@example.com")
        viewModel.onContinueClick()
        advanceUntilIdle()

        assertEquals("Registration failed", viewModel.uiState.value.errorMessage)
        assertNull(sessionProvider.currentToken())
    }

    @Test
    fun `storage write failure keeps runtime authenticated session`() = runTest {
        val authenticator = FakePasskeyAuthenticator(loginResult = Result.success("login-token"))
        val sessionProvider = createSessionProvider(FailingWriteSecureTokenStore())
        val viewModel = AuthViewModel(
            passkeyAuthenticator = authenticator,
            currentSessionProvider = sessionProvider,
        )

        viewModel.onEmailChange("hello@example.com")
        viewModel.onContinueClick()
        advanceUntilIdle()

        assertEquals("login-token", sessionProvider.currentToken())
        assertNull(viewModel.uiState.value.errorMessage)
    }
}

private fun createSessionProvider(tokenStore: SecureTokenStore): CurrentSessionProviderImpl =
    CurrentSessionProviderImpl(
        tokenStore = tokenStore,
        readArticlesCache = InMemoryReadArticlesCache(),
    )

private class FakePasskeyAuthenticator(
    private val loginResult: Result<String> = Result.success("login-token"),
    private val registerResult: Result<String> = Result.success("register-token"),
) : PasskeyAuthenticator {
    var loginCalls = 0
        private set
    var registerCalls = 0
        private set
    var loginEmail: String? = null
        private set
    var registerEmail: String? = null
        private set

    override suspend fun login(email: String): String {
        loginCalls += 1
        loginEmail = email
        return loginResult.getOrThrow()
    }

    override suspend fun register(email: String): String {
        registerCalls += 1
        registerEmail = email
        return registerResult.getOrThrow()
    }
}

private class FailingWriteSecureTokenStore : SecureTokenStore {
    override suspend fun readToken(): String? = null

    override suspend fun writeToken(token: String) {
        error("Write failed")
    }

    override suspend fun clearToken() = Unit
}
