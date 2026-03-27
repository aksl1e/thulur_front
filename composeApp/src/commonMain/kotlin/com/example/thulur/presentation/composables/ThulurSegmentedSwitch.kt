package com.example.thulur.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThulurSegmentedSwitchSemanticColors
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.rememberThulurSegmentedSwitchSemanticColors
import com.example.thulur.presentation.theme.thulurDp

typealias ThulurSegmentedSwitchColors = ThulurSegmentedSwitchSemanticColors

private data class SegmentBounds(
    val offset: Dp,
    val width: Dp,
)

object ThulurSegmentedSwitchDefaults {
    @Composable
    @ReadOnlyComposable
    fun colors(
        colorRole: ThulurColorRole,
        enabled: Boolean,
        colorsOverride: ThulurSegmentedSwitchSemanticColors? = null,
    ): ThulurSegmentedSwitchColors = rememberThulurSegmentedSwitchSemanticColors(
        colorRole = colorRole,
        enabled = enabled,
        colorsOverride = colorsOverride,
    )
}

@Composable
fun <T> ThulurSegmentedSwitch(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorRole: ThulurColorRole = ThulurColorRole.Primary,
    optionLabel: (T) -> String,
    textStyle: TextStyle = ThulurTheme.Typography.bodyLarge,
    colorsOverride: ThulurSegmentedSwitchSemanticColors? = null,
    cornerRadius: Dp = 25.thulurDp(),
    horizontalItemPadding: Dp = 16.thulurDp(),
    verticalItemPadding: Dp = 0.dp,
) {
    if (options.isEmpty()) return

    val density = LocalDensity.current
    val segmentBounds = remember(options) { mutableStateMapOf<Int, SegmentBounds>() }
    val colors = ThulurSegmentedSwitchDefaults.colors(
        colorRole = colorRole,
        enabled = enabled,
        colorsOverride = colorsOverride,
    )
    val containerHeight = remember { mutableStateOf(0.dp) }
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val selectedBounds = segmentBounds[selectedIndex]
    val animatedOffset by animateDpAsState(
        targetValue = selectedBounds?.offset ?: 0.dp,
        label = "thulurSegmentedSwitchOffset",
    )
    val animatedWidth by animateDpAsState(
        targetValue = selectedBounds?.width ?: 0.dp,
        label = "thulurSegmentedSwitchWidth",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.containerColor),
    ) {
        if (selectedBounds != null && containerHeight.value > 0.dp) {
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset, y = 0.dp)
                    .width(animatedWidth)
                    .height(containerHeight.value)
                    .clip(shape)
                    .background(colors.selectedContainerColor),
            )
        }

        Row(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                containerHeight.value = with(density) { coordinates.size.height.toDp() }
            },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                val optionSelected = option == selected
                val textColor by animateColorAsState(
                    targetValue = when {
                        !enabled -> colors.disabledContentColor
                        optionSelected -> colors.selectedContentColor
                        else -> colors.unselectedContentColor
                    },
                    label = "thulurSegmentedSwitchTextColor",
                )

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .clickable(
                            enabled = enabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onSelect(option)
                        }
                        .onGloballyPositioned { coordinates ->
                            val offset = with(density) {
                                coordinates.positionInParent().x.toDp()
                            }
                            val width = with(density) {
                                coordinates.size.width.toDp()
                            }
                            segmentBounds[index] = SegmentBounds(
                                offset = offset,
                                width = width,
                            )
                        }
                        .padding(
                            horizontal = horizontalItemPadding,
                            vertical = verticalItemPadding,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = optionLabel(option),
                        style = textStyle.copy(color = textColor),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThulurSegmentedSwitchPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            Box(
                modifier = Modifier
                    .background(ThulurTheme.Colors.slate.s100)
                    .padding(16.dp),
            ) {
                TopicsSwitch(
                    selected = TopicsViewMode.TopicsAndArticles,
                    onSelect = {},
                )
            }
        }
    }
}
