package com.example.thulur.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurTextField
import com.example.thulur.presentation.root.RootLoadingScreen
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onContinueClick = viewModel::onContinueClick,
        onTroubleSigningInClick = viewModel::onTroubleSigningInClick,
    )
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    onTroubleSigningInClick: () -> Unit,
) {
    if (uiState.isSubmitting) {
        RootLoadingScreen()
        return
    }

    val colors = ThulurTheme.SemanticColors.authScreen
    val typography = ThulurTheme.SemanticTypography
    val cardShape = RoundedCornerShape(20.thulurDp())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBackground)
            .padding(10.thulurDp()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(colors.cardContainer, cardShape)
                .border(
                    width = 0.3.dp,
                    color = colors.cardBorder,
                    shape = cardShape,
                )
                .padding(
                    start = 60.thulurDp(),
                    top = 60.thulurDp(),
                    end = 60.thulurDp(),
                    bottom = 30.thulurDp(),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(60.thulurDp()),
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                tint = colors.iconColor,
                modifier = Modifier.size(65.thulurDp()),
            )

            Column(
                modifier = Modifier.width(440.thulurDp()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.thulurDp()),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.thulurDp()),
                ) {
                    BasicText(
                        text = "Sign in",
                        style = typography.authTitle.copy(
                            color = colors.titleColor,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    BasicText(
                        text = "Use your biometric data or hardware key to continue",
                        style = typography.authSubtitle.copy(
                            color = colors.subtitleColor,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(30.thulurDp()),
                ) {
                    ThulurTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        placeholder = "Email",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.thulurDp()),
                        enabled = !uiState.isSubmitting,
                        isError = uiState.errorMessage != null,
                        textStyle = typography.authInputText,
                        placeholderStyle = typography.authInputPlaceholder,
                        stateColorsOverride = colors.emailField,
                    )

                    ThulurButton(
                        text = "Continue with Passkey",
                        onClick = onContinueClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isContinueEnabled,
                        textStyle = typography.authPrimaryAction,
                        shape = RoundedCornerShape(7.thulurDp()),
                        contentPadding = PaddingValues(vertical = 15.thulurDp()),
                        stateColorsOverride = colors.continueButton,
                    )

                    uiState.errorMessage?.let { message ->
                        BasicText(
                            text = message,
                            style = typography.authSubtitle.copy(
                                color = colors.errorColor,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }

            ThulurButton(
                text = "Trouble signing in?",
                onClick = onTroubleSigningInClick,
                enabled = !uiState.isSubmitting,
                useContainerStates = false,
                textStyle = typography.authTroubleLink,
                contentPadding = PaddingValues(0.dp),
                stateColorsOverride = colors.troubleLinkButton,
            )
        }
    }
}

@Preview
@Composable
private fun AuthScreenPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            AuthScreen(
                uiState = AuthUiState(),
                onEmailChange = {},
                onContinueClick = {},
                onTroubleSigningInClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun AuthScreenSubmittingPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            AuthScreen(
                uiState = AuthUiState(
                    email = "hello@example.com",
                    isSubmitting = true,
                ),
                onEmailChange = {},
                onContinueClick = {},
                onTroubleSigningInClick = {},
            )
        }
    }
}
