package com.example.thulur.presentation.auth

data class AuthUiState(
    val email: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val isContinueEnabled: Boolean
        get() = email.isNotBlank() && !isSubmitting
}
