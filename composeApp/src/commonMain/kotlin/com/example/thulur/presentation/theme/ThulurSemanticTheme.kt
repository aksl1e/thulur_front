package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class ThulurTextButtonSemanticColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Immutable
data class ThulurSegmentedSwitchSemanticColors(
    val containerColor: Color,
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val disabledContentColor: Color,
)

@Immutable
data class ThulurAppBarSemanticColors(
    val containerColor: Color,
    val backAreaColor: Color,
    val titleColor: Color,
    val brandColor: Color,
    val settingsTint: Color,
)

@Immutable
data class ThulurSemanticColors(
    val appBar: ThulurAppBarSemanticColors,
)

@Immutable
data class ThulurSemanticTypography(
    val appBarBackLabel: TextStyle,
    val appBarTitle: TextStyle,
    val appBarBrand: TextStyle,
    val topicsSwitchLabel: TextStyle,
)

@Composable
@ReadOnlyComposable
fun rememberThulurAppBarSemanticColors(): ThulurAppBarSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurAppBarSemanticColors(
            containerColor = slate.s100,
            backAreaColor = slate.s300,
            titleColor = slate.s950,
            brandColor = slate.s950,
            settingsTint = slate.s950,
        )

        ThemeMode.Dark -> ThulurAppBarSemanticColors(
            containerColor = slate.s900,
            backAreaColor = slate.s700,
            titleColor = slate.s50,
            brandColor = slate.s50,
            settingsTint = slate.s50,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurTextButtonSemanticColors(
    colorRole: ThulurColorRole,
    enabled: Boolean,
    isHovered: Boolean,
    isPressed: Boolean,
    isFocused: Boolean,
    useContainerStates: Boolean,
): ThulurTextButtonSemanticColors {
    val roleScale = rememberThulurShadeScale(colorRole)
    val slate = ThulurTheme.Colors.slate

    if (!useContainerStates) {
        return when (ThulurTheme.Mode) {
            ThemeMode.Light -> when {
                !enabled -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s300,
                )

                isPressed -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s900,
                )

                isHovered || isFocused -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s700,
                )

                else -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )
            }

            ThemeMode.Dark -> when {
                !enabled -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )

                isPressed -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s50,
                )

                isHovered || isFocused -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s100,
                )

                else -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s300,
                )
            }
        }
    }

    return when {
        !enabled -> ThulurTextButtonSemanticColors(
            containerColor = Color.Transparent,
            contentColor = roleScale.s300,
        )

        isPressed -> ThulurTextButtonSemanticColors(
            containerColor = roleScale.s700,
            contentColor = slate.s50,
        )

        isHovered || isFocused -> ThulurTextButtonSemanticColors(
            containerColor = roleScale.s500,
            contentColor = slate.s50,
        )

        else -> ThulurTextButtonSemanticColors(
            containerColor = Color.Transparent,
            contentColor = roleScale.s500,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSegmentedSwitchSemanticColors(
    colorRole: ThulurColorRole,
    enabled: Boolean,
): ThulurSegmentedSwitchSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val selectedScale = rememberThulurShadeScale(colorRole)

    val containerColor = when (ThulurTheme.Mode) {
        ThemeMode.Light -> slate.s300
        ThemeMode.Dark -> slate.s700
    }
    val unselectedContentColor = when (ThulurTheme.Mode) {
        ThemeMode.Light -> slate.s700
        ThemeMode.Dark -> slate.s300
    }

    return if (enabled) {
        ThulurSegmentedSwitchSemanticColors(
            containerColor = containerColor,
            selectedContainerColor = selectedScale.s500,
            selectedContentColor = slate.s50,
            unselectedContentColor = unselectedContentColor,
            disabledContentColor = selectedScale.s300,
        )
    } else {
        ThulurSegmentedSwitchSemanticColors(
            containerColor = containerColor,
            selectedContainerColor = selectedScale.s300,
            selectedContentColor = slate.s50,
            unselectedContentColor = selectedScale.s300,
            disabledContentColor = selectedScale.s300,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSemanticTypography(): ThulurSemanticTypography = ThulurSemanticTypography(
    appBarBackLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.ExtraLight,
        fontSize = 24.thulurSp(),
        lineHeight = 32.thulurSp(),
    ),
    appBarTitle = ThulurTheme.Typography.headlineLarge.copy(
        fontFamily = ThulurTheme.Typography.bodyLarge.fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.thulurSp(),
        lineHeight = 42.thulurSp(),
    ),
    appBarBrand = ThulurTheme.Typography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 32.thulurSp(),
        lineHeight = 40.thulurSp(),
    ),
    topicsSwitchLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 18.thulurSp(),
        lineHeight = 18.thulurSp(),
    ),
)

@Composable
@ReadOnlyComposable
fun rememberThulurSemanticColors(): ThulurSemanticColors = ThulurSemanticColors(
    appBar = rememberThulurAppBarSemanticColors(),
)
