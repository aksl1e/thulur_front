package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalThulurMode = staticCompositionLocalOf { ThemeMode.Light }
private val LocalThulurTypography = staticCompositionLocalOf { fallbackThulurTypography() }

object ThulurTheme {
    val Colors: ThulurColors = DefaultThulurColors

    val SemanticColors: ThulurSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = rememberThulurSemanticColors()

    val SemanticTypography: ThulurSemanticTypography
        @Composable
        @ReadOnlyComposable
        get() = rememberThulurSemanticTypography()

    val Typography: ThulurTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalThulurTypography.current

    val Mode: ThemeMode
        @Composable
        @ReadOnlyComposable
        get() = LocalThulurMode.current

    @Composable
    operator fun invoke(
        mode: ThemeMode,
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(
            LocalThulurMode provides mode,
            LocalThulurTypography provides rememberThulurTypography(),
            content = content,
        )
    }
}
