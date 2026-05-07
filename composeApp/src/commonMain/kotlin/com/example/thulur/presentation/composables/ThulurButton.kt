package com.example.thulur.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThulurButtonSemanticColors
import com.example.thulur.presentation.theme.ThulurButtonStateSemanticColors
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.rememberThulurButtonSemanticColors
import com.example.thulur.presentation.theme.thulurDefaultShape
import com.example.thulur.presentation.theme.thulurDp

enum class ThulurButtonContentDirection {
    Horizontal,
    Vertical,
}

@Composable
fun ThulurButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    supportingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colorRole: ThulurColorRole = ThulurColorRole.Primary,
    useContainerStates: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = ThulurTheme.Typography.bodyLarge,
    supportingTextStyle: TextStyle? = null,
    shape: Shape = thulurDefaultShape(),
    contentPadding: PaddingValues = PaddingValues(15.thulurDp()),
    iconSize: Dp? = null,
    spacing: Dp = 8.thulurDp(),
    contentDirection: ThulurButtonContentDirection = ThulurButtonContentDirection.Horizontal,
    contentHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    stateColorsOverride: ThulurButtonStateSemanticColors? = null,
    contentDescription: String? = null,
    tooltipText: String? = null,
) {
    require(text != null || leadingIcon != null || trailingIcon != null) {
        "ThulurButton requires text or at least one icon."
    }
    require(text != null || supportingText == null) {
        "supportingText requires text."
    }

    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val colors = rememberThulurButtonColors(
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
        label = "thulurButtonContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = colors.contentColor,
        label = "thulurButtonContentColor",
    )
    val tooltipVisible = enabled && tooltipText != null && isHovered
    val tooltipOpacity by animateFloatAsState(
        targetValue = if (tooltipVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "thulurButtonTooltipAlpha",
    )

    CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
        Box(contentAlignment = Alignment.Center) {
            ThulurButtonTooltip(
                text = tooltipText,
                opacity = tooltipOpacity,
            )

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
                    .then(
                        if (contentDescription != null) {
                            Modifier.semantics {
                                this.contentDescription = contentDescription
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (contentDirection) {
                    ThulurButtonContentDirection.Horizontal -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            leadingIcon?.let { IconSlot(it, iconSize) }
                            ButtonTextBlock(
                                text = text,
                                supportingText = supportingText,
                                textStyle = textStyle,
                                supportingTextStyle = resolvedSupportingTextStyle,
                                textAlign = contentHorizontalAlignment.toTextAlign(),
                                contentColor = animatedContentColor,
                                spacing = spacing,
                                horizontalAlignment = contentHorizontalAlignment,
                            )
                            trailingIcon?.let { IconSlot(it, iconSize) }
                        }
                    }

                    ThulurButtonContentDirection.Vertical -> {
                        Column(
                            horizontalAlignment = contentHorizontalAlignment,
                            verticalArrangement = Arrangement.spacedBy(spacing),
                        ) {
                            if (leadingIcon != null || trailingIcon != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(spacing),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    leadingIcon?.let { IconSlot(it, iconSize) }
                                    trailingIcon?.let { IconSlot(it, iconSize) }
                                }
                            }

                            ButtonTextBlock(
                                text = text,
                                supportingText = supportingText,
                                textStyle = textStyle,
                                supportingTextStyle = resolvedSupportingTextStyle,
                                textAlign = contentHorizontalAlignment.toTextAlign(),
                                contentColor = animatedContentColor,
                                spacing = spacing,
                                horizontalAlignment = contentHorizontalAlignment,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun rememberThulurButtonColors(
    colorRole: ThulurColorRole,
    enabled: Boolean,
    isHovered: Boolean,
    isPressed: Boolean,
    useContainerStates: Boolean,
    stateColorsOverride: ThulurButtonStateSemanticColors? = null,
): ThulurButtonSemanticColors = rememberThulurButtonSemanticColors(
    colorRole = colorRole,
    enabled = enabled,
    isHovered = isHovered,
    isPressed = isPressed,
    useContainerStates = useContainerStates,
    stateColorsOverride = stateColorsOverride,
)

@Composable
private fun ThulurButtonTooltip(
    text: String?,
    opacity: Float,
) {
    if (text == null) return

    val tooltipColors = ThulurTheme.SemanticColors.buttonTooltip
    val tooltipTextStyle = ThulurTheme.SemanticTypography.buttonTooltip
    val tooltipGapPx = with(LocalDensity.current) { 8.thulurDp().roundToPx() }
    var tooltipHeightPx by remember { mutableStateOf(0) }

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, -(tooltipHeightPx + tooltipGapPx)),
    ) {
        val shape = thulurDefaultShape()

        Box(
            modifier = Modifier
                .alpha(opacity)
                .onSizeChanged { tooltipHeightPx = it.height }
                .clip(shape)
                .background(tooltipColors.containerColor)
                .padding(
                    horizontal = 10.thulurDp(),
                    vertical = 6.thulurDp(),
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = text,
                style = tooltipTextStyle.copy(
                    color = tooltipColors.contentColor,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun IconSlot(icon: @Composable () -> Unit, size: Dp?) {
    if (size != null) {
        Box(modifier = Modifier.size(size)) { icon() }
    } else {
        icon()
    }
}

@Composable
private fun ButtonTextBlock(
    text: String?,
    supportingText: String?,
    textStyle: TextStyle,
    supportingTextStyle: TextStyle,
    textAlign: TextAlign,
    contentColor: androidx.compose.ui.graphics.Color,
    spacing: Dp,
    horizontalAlignment: Alignment.Horizontal,
) {
    if (text == null) return

    if (supportingText != null) {
        Column(
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            BasicText(
                text = text,
                style = textStyle.copy(
                    color = contentColor,
                    textAlign = textAlign,
                ),
            )
            BasicText(
                text = supportingText,
                style = supportingTextStyle.copy(
                    color = contentColor,
                    textAlign = textAlign,
                ),
            )
        }
    } else {
        BasicText(
            text = text,
            style = textStyle.copy(
                color = contentColor,
                textAlign = textAlign,
            ),
        )
    }
}

private fun Alignment.Horizontal.toTextAlign(): TextAlign = when (this) {
    Alignment.Start -> TextAlign.Start
    Alignment.End -> TextAlign.End
    else -> TextAlign.Center
}

@Preview
@Composable
private fun ThulurButtonPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            Box(
                modifier = Modifier
                    .background(ThulurTheme.Colors.slate.s100)
                    .padding(16.dp),
            ) {
                ThulurButton(
                    text = "Yesterday",
                    onClick = {},
                    colorRole = ThulurColorRole.Slate,
                    useContainerStates = false,
                    tooltipText = "Yesterday",
                )
            }
        }
    }
}
