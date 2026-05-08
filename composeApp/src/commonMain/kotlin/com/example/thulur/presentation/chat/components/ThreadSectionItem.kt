package com.example.thulur.presentation.chat.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThreadSectionItem(
    label: String,
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
        label = "threadSectionItemContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabled.contentColor
            selected -> colors.selected.contentColor
            isPressed -> colors.pressed.contentColor
            isHovered -> colors.hovered.contentColor
            else -> colors.rest.contentColor
        },
        label = "threadSectionItemContentColor",
    )
    val hoveredOverlayAlpha by animateFloatAsState(
        targetValue = if (enabled && !selected && isHovered && !isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "threadSectionItemHoveredOverlayAlpha",
    )
    val pressedOverlayAlpha by animateFloatAsState(
        targetValue = if (enabled && !selected && isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "threadSectionItemPressedOverlayAlpha",
    )

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
        BasicText(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            style = typography.settingsSelectorLabel.copy(
                color = animatedContentColor,
            ),
        )
    }
}