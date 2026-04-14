package com.example.thulur.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.LocalContentColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThulurTextButtonSemanticColors
import com.example.thulur.presentation.theme.ThulurTextButtonStateSemanticColors
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.rememberThulurTextButtonSemanticColors
import com.example.thulur.presentation.theme.thulurDp

typealias ThulurTextButtonColors = ThulurTextButtonSemanticColors

enum class ThulurTextButtonContentDirection {
    Horizontal,
    Vertical,
}

object ThulurTextButtonDefaults {
    @Composable
    @ReadOnlyComposable
    fun colors(
        colorRole: ThulurColorRole,
        enabled: Boolean,
        isHovered: Boolean,
        isPressed: Boolean,
        useContainerStates: Boolean,
        stateColorsOverride: ThulurTextButtonStateSemanticColors? = null,
    ): ThulurTextButtonColors = rememberThulurTextButtonSemanticColors(
        colorRole = colorRole,
        enabled = enabled,
        isHovered = isHovered,
        isPressed = isPressed,
        useContainerStates = useContainerStates,
        stateColorsOverride = stateColorsOverride,
    )
}

@Composable
fun ThulurTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorRole: ThulurColorRole = ThulurColorRole.Primary,
    useContainerStates: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = ThulurTheme.Typography.bodyLarge,
    supportingTextStyle: TextStyle? = null,
    shape: Shape = RoundedCornerShape(0.dp),
    contentPadding: PaddingValues = PaddingValues(15.thulurDp()),
    spacing: Dp = 8.thulurDp(),
    contentDirection: ThulurTextButtonContentDirection = ThulurTextButtonContentDirection.Horizontal,
    contentHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    stateColorsOverride: ThulurTextButtonStateSemanticColors? = null,
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val colors = ThulurTextButtonDefaults.colors(
        colorRole = colorRole,
        enabled = enabled,
        isHovered = isHovered,
        isPressed = isPressed,
        useContainerStates = useContainerStates,
        stateColorsOverride = stateColorsOverride,
    )
    val resolvedSupportingTextStyle = supportingTextStyle ?: textStyle

    val animatedContainerColor by animateColorAsState(
        targetValue = colors.containerColor,
        label = "thulurTextButtonContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = colors.contentColor,
        label = "thulurTextButtonContentColor",
    )

    CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(animatedContainerColor)
                .hoverable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (contentDirection) {
                ThulurTextButtonContentDirection.Horizontal -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        leadingIcon?.invoke()
                        if (supportingText != null) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(spacing),
                            ) {
                                BasicText(
                                    text = text,
                                    style = textStyle.copy(color = animatedContentColor),
                                )
                                BasicText(
                                    text = supportingText,
                                    style = resolvedSupportingTextStyle.copy(color = animatedContentColor),
                                )
                            }
                        } else {
                            BasicText(
                                text = text,
                                style = textStyle.copy(color = animatedContentColor),
                            )
                        }
                    }
                }

                ThulurTextButtonContentDirection.Vertical -> {
                    val textAlign = contentHorizontalAlignment.toTextAlign()

                    Column(
                        horizontalAlignment = contentHorizontalAlignment,
                        verticalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        leadingIcon?.invoke()
                        Column(
                            horizontalAlignment = contentHorizontalAlignment,
                            verticalArrangement = Arrangement.spacedBy(spacing),
                        ) {
                            BasicText(
                                text = text,
                                style = textStyle.copy(
                                    color = animatedContentColor,
                                    textAlign = textAlign,
                                ),
                            )
                            supportingText?.let {
                                BasicText(
                                    text = it,
                                    style = resolvedSupportingTextStyle.copy(
                                        color = animatedContentColor,
                                        textAlign = textAlign,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Alignment.Horizontal.toTextAlign(): TextAlign = when (this) {
    Alignment.Start -> TextAlign.Start
    Alignment.End -> TextAlign.End
    else -> TextAlign.Center
}

@Preview
@Composable
private fun ThulurTextButtonPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            Box(
                modifier = Modifier
                    .background(ThulurTheme.Colors.slate.s100)
                    .padding(16.dp),
            ) {
                ThulurTextButton(
                    text = "Yesterday",
                    onClick = {},
                    colorRole = ThulurColorRole.Slate,
                    useContainerStates = false,
                )
            }
        }
    }
}
