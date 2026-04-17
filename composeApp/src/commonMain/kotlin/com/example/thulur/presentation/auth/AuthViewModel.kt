package com.example.thulur.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.auth.PasskeyAuthenticationErrorCode
import com.example.thulur.domain.auth.PasskeyAuthenticationException
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur.domain.session.CurrentSessionProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val passkeyAuthenticator: PasskeyAuthenticator,
    private val currentSessionProvider: CurrentSessionProvider,
) : ViewModel() {
    private var submitJob: Job? = null

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                errorMessage = null,
            )
        }
    }

    fun onContinueClick() {
        if (submitJob?.isActive == true) return

        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Enter your email to continue.")
            }
            return
        }

        submitJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    email = email,
                    isSubmitting = true,
                    errorMessage = null,
                )
            }

            try {
                val token = try {
                    passkeyAuthenticator.login(email = email)
                } catch (exception: PasskeyAuthenticationException) {
                    if (exception.code == PasskeyAuthenticationErrorCode.UserNotFound) {
                        passkeyAuthenticator.register(email = email)
                    } else {
                        throw exception
                    }
                }

                if (token.isBlank()) {
                    error("Authentication finished without a valid token.")
                }

                currentSessionProvider.updateToken(token)
                _uiState.update { state ->
                    state.copy(isSubmitting = false)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "Passkey sign-in failed.",
                    )
                }
            }
        }
    }

    fun onTroubleSigningInClick() {
        _uiState.update { state ->
            state.copy(errorMessage = "Account recovery is not available yet.")
        }
    }
}
