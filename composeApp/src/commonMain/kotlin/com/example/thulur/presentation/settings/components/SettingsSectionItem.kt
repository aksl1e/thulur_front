package com.example.thulur.presentation.settings.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ThulurButtonSemanticColors
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSectionItem(
    label: String,
    trailingIcon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(10.thulurDp()),
) {
    val colors = ThulurTheme.SemanticColors.settingsSectionItem
    val typography = ThulurTheme.SemanticTypography
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(
        topStart = 15.thulurDp(),
        bottomStart = 15.thulurDp(),
        topEnd = 0.thulurDp(),
        bottomEnd = 0.thulurDp(),
    )
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val resolvedColors = when {
        !enabled -> colors.disabled
        selected -> colors.selected
        else -> colors.rest
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = resolvedColors.containerColor,
        label = "settingsSectionItemContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabled.contentColor
            selected -> colors.selected.contentColor
            isPressed -> colors.pressed.contentColor
            isHovered -> colors.hovered.contentColor
            else -> colors.rest.contentColor
        },
        label = "settingsSectionItemContentColor",
    )
    val hoveredOverlayAlpha by animateFloatAsState(
        targetValue = if (enabled && !selected && isHovered && !isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "settingsSectionItemHoveredOverlayAlpha",
    )
    val pressedOverlayAlpha by animateFloatAsState(
        targetValue = if (enabled && !selected && isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "settingsSectionItemPressedOverlayAlpha",
    )

    CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
        Box(
            modifier = modifier
                .clip(shape)
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
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(animatedContainerColor),
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        colors.hovered.containerColor.copy(alpha = hoveredOverlayAlpha),
                    ),
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        colors.pressed.containerColor.copy(alpha = pressedOverlayAlpha),
                    ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 10.thulurDp(),
                    alignment = Alignment.End,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = label,
                    style = typography.settingsSelectorLabel.copy(
                        color = animatedContentColor,
                    ),
                )
                trailingIcon()
            }
        }
    }
}

@Preview
@Composable
private fun SettingsSectionItemLightPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            SettingsSectionItem(
                label = "Account & App",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                    )
                },
                selected = true,
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.6f),
            )
        }
    }
}

@Preview
@Composable
private fun SettingsSectionItemDarkPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Dark) {
            SettingsSectionItem(
                label = "Feeds",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                    )
                },
                selected = false,
                enabled = false,
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .alpha(1f),
            )
        }
    }
}
