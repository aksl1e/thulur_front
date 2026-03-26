package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

enum class ThulurColorRole {
    Primary,
    Error,
    Success,
    Warning,
    Slate,
}

internal fun ThulurColors.resolveShadeScale(role: ThulurColorRole): ThulurShadeScale = when (role) {
    ThulurColorRole.Primary -> primary
    ThulurColorRole.Error -> error
    ThulurColorRole.Success -> success
    ThulurColorRole.Warning -> warning
    ThulurColorRole.Slate -> slate
}

@Composable
@ReadOnlyComposable
fun rememberThulurShadeScale(role: ThulurColorRole): ThulurShadeScale = ThulurTheme.Colors.resolveShadeScale(role)
