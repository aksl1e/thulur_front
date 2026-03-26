package com.example.thulur.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.LocalContentColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThulurTextButtonSemanticColors
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.rememberThulurTextButtonSemanticColors

typealias ThulurTextButtonColors = ThulurTextButtonSemanticColors

object ThulurTextButtonDefaults {
    @Composable
    @ReadOnlyComposable
    fun colors(
        colorRole: ThulurColorRole,
        enabled: Boolean,
        isHovered: Boolean,
        isPressed: Boolean,
        isFocused: Boolean,
        useContainerStates: Boolean,
    ): ThulurTextButtonColors = rememberThulurTextButtonSemanticColors(
        colorRole = colorRole,
        enabled = enabled,
        isHovered = isHovered,
        isPressed = isPressed,
        isFocused = isFocused,
        useContainerStates = useContainerStates,
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textStyle: TextStyle = ThulurTheme.Typography.bodyLarge,
    shape: Shape = RoundedCornerShape(999.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    spacing: Dp = 8.dp,
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val colors = ThulurTextButtonDefaults.colors(
        colorRole = colorRole,
        enabled = enabled,
        isHovered = isHovered,
        isPressed = isPressed,
        isFocused = isFocused,
        useContainerStates = useContainerStates,
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = colors.containerColor,
        label = "thulurTextButtonContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = colors.contentColor,
        label = "thulurTextButtonContentColor",
    )

    CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
        Row(
            modifier = modifier
                .clip(shape)
                .background(animatedContainerColor)
                .hoverable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                )
                .focusable(
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
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()
            BasicText(
                text = text,
                style = textStyle.copy(color = animatedContentColor),
            )
        }
    }
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
                    modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                )
            }
        }
    }
}
