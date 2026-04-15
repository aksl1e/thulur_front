package com.example.thulur.presentation.root

import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.data.session.InMemorySecureTokenStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AppRootViewModelTest {
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
    fun `starts in loading state before reading persisted token`() = runTest {
        val viewModel = AppRootViewModel(CurrentSessionProviderImpl(InMemorySecureTokenStore()))

        assertEquals(AppRootUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `moves to unauthenticated when no persisted token exists`() = runTest {
        val viewModel = AppRootViewModel(CurrentSessionProviderImpl(InMemorySecureTokenStore()))

        advanceUntilIdle()

        assertEquals(AppRootUiState.Unauthenticated, viewModel.uiState.value)
    }

    @Test
    fun `moves to authenticated when persisted token exists`() = runTest {
        val sessionProvider = CurrentSessionProviderImpl(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = AppRootViewModel(sessionProvider)

        advanceUntilIdle()

        assertEquals(AppRootUiState.Authenticated(sessionInstanceId = 1), viewModel.uiState.value)
    }

    @Test
    fun `moves back to unauthenticated when token is cleared`() = runTest {
        val sessionProvider = CurrentSessionProviderImpl(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = AppRootViewModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.clearToken()
        advanceUntilIdle()

        assertEquals(AppRootUiState.Unauthenticated, viewModel.uiState.value)
    }

    @Test
    fun `increments session instance id when user authenticates again after logout`() = runTest {
        val sessionProvider = CurrentSessionProviderImpl(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = AppRootViewModel(sessionProvider)
        advanceUntilIdle()

        assertEquals(AppRootUiState.Authenticated(sessionInstanceId = 1), viewModel.uiState.value)

        sessionProvider.clearToken()
        advanceUntilIdle()
        assertEquals(AppRootUiState.Unauthenticated, viewModel.uiState.value)

        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Authenticated(sessionInstanceId = 2), viewModel.uiState.value)
    }

    @Test
    fun `does not increment session instance id when token changes inside active session`() = runTest {
        val sessionProvider = CurrentSessionProviderImpl(InMemorySecureTokenStore(initialToken = "token-1"))
        val viewModel = AppRootViewModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Authenticated(sessionInstanceId = 1), viewModel.uiState.value)
    }

    @Test
    fun `reads session instance id from current session provider`() = runTest {
        val sessionProvider = CurrentSessionProviderImpl(InMemorySecureTokenStore())
        val viewModel = AppRootViewModel(sessionProvider)
        advanceUntilIdle()

        sessionProvider.updateToken("token-1")
        advanceUntilIdle()
        assertEquals(AppRootUiState.Authenticated(sessionInstanceId = 1), viewModel.uiState.value)

        sessionProvider.clearToken()
        sessionProvider.updateToken("token-2")
        advanceUntilIdle()

        assertEquals(AppRootUiState.Authenticated(sessionInstanceId = 2), viewModel.uiState.value)
    }
}
