package com.example.thulur.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ThulurTimeSelector(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ThulurTheme.SemanticColors.settingsTimeSelector
    val typography = ThulurTheme.SemanticTypography

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.thulurDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeSelectorSegment(
            value = hour.coerceIn(HOUR_RANGE.first, HOUR_RANGE.last),
            onValueChange = { onTimeChange(it, minute) },
            valueRange = HOUR_RANGE,
            enabled = enabled,
        )

        BasicText(
            text = ":",
            style = typography.settingsTimeValue.copy(
                color = colors.dividerColor,
                textAlign = TextAlign.Center,
            ),
        )

        TimeSelectorSegment(
            value = minute.coerceIn(MINUTE_RANGE.first, MINUTE_RANGE.last),
            onValueChange = { onTimeChange(hour, it) },
            valueRange = MINUTE_RANGE,
            enabled = enabled,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TimeSelectorSegment(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    enabled: Boolean,
) {
    val colors = ThulurTheme.SemanticColors.settingsTimeSelector
    val typography = ThulurTheme.SemanticTypography
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val containerColor = when {
        !enabled -> colors.segmentDisabledContainerColor
        isHovered -> colors.segmentHoveredContainerColor
        else -> colors.segmentContainerColor
    }
    val contentColor = if (enabled) {
        colors.segmentContentColor
    } else {
        colors.segmentDisabledContentColor
    }

    Box(
        modifier = Modifier
            .width(56.thulurDp())
            .height(64.thulurDp())
            .clip(RoundedCornerShape(18.thulurDp()))
            .background(containerColor)
            .hoverable(
                enabled = enabled,
                interactionSource = interactionSource,
            )
            .onPointerEvent(PointerEventType.Scroll) { event ->
                if (!enabled) return@onPointerEvent
                val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: return@onPointerEvent
                when {
                    delta < 0f -> onValueChange(value.wrapNext(valueRange))
                    delta > 0f -> onValueChange(value.wrapPrevious(valueRange))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = value.toString(),
            style = typography.settingsTimeValue.copy(
                color = contentColor,
                textAlign = TextAlign.Center,
            ),
        )

        if (isHovered && enabled) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimeSelectorArrowButton(
                    onClick = { onValueChange(value.wrapNext(valueRange)) },
                    enabled = enabled,
                    isIncrement = true,
                )
                TimeSelectorArrowButton(
                    onClick = { onValueChange(value.wrapPrevious(valueRange)) },
                    enabled = enabled,
                    isIncrement = false,
                )
            }
        }
    }
}

@Composable
private fun TimeSelectorArrowButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isIncrement: Boolean,
) {
    val colors = ThulurTheme.SemanticColors.settingsTimeSelector
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val arrowColor = when {
        !enabled -> colors.arrowDisabledColor
        isHovered -> colors.arrowHoveredColor
        else -> colors.arrowRestColor
    }

    CompositionLocalProvider(LocalContentColor provides arrowColor) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.thulurDp())
                .hoverable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isIncrement) {
                    Icons.Outlined.KeyboardArrowUp
                } else {
                    Icons.Outlined.KeyboardArrowDown
                },
                contentDescription = null,
                modifier = Modifier.size(18.thulurDp()),
                tint = arrowColor,
            )
        }
    }
}

private fun Int.wrapNext(range: IntRange): Int = if (this >= range.last) {
    range.first
} else {
    this + 1
}

private fun Int.wrapPrevious(range: IntRange): Int = if (this <= range.first) {
    range.last
} else {
    this - 1
}

private val HOUR_RANGE = 0..24
private val MINUTE_RANGE = 0..59

@Preview
@Composable
private fun ThulurTimeSelectorLightPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .padding(24.thulurDp()),
            ) {
                ThulurTimeSelector(
                    hour = 8,
                    minute = 45,
                    onTimeChange = { _, _ -> },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ThulurTimeSelectorDarkPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Dark) {
            Box(
                modifier = Modifier
                    .background(ThulurTheme.Colors.slate.s950)
                    .padding(24.thulurDp()),
            ) {
                ThulurTimeSelector(
                    hour = 22,
                    minute = 5,
                    onTimeChange = { _, _ -> },
                )
            }
        }
    }
}
