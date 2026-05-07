package com.example.thulur.presentation.dailyfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDefaultShape

data class DailyFeedColors(
    val surface: Color,
    val surfaceContainer: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val accent: Color,
    val onAccent: Color,
)

@Composable
internal fun DailyFeedStatusCard(
    title: String,
    body: String,
    colors: DailyFeedColors,
    modifier: Modifier = Modifier,
) {
    val shape = thulurDefaultShape()

    Column(
        modifier = modifier
            .background(colors.surfaceContainer, shape)
            .border(1.dp, colors.outline, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText(
            text = title,
            style = ThulurTheme.Typography.headlineMedium.copy(color = colors.onSurface),
        )
        BasicText(
            text = body,
            style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
        )
    }
}

@Composable
internal fun DailyFeedErrorCard(
    title: String,
    message: String,
    colors: DailyFeedColors,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = thulurDefaultShape()

    Column(
        modifier = modifier
            .background(colors.surfaceContainer, shape)
            .border(1.dp, colors.outline, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BasicText(
            text = title,
            style = ThulurTheme.Typography.headlineMedium.copy(color = colors.onSurface),
        )
        BasicText(
            text = message,
            style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
        )
        RetryButton(colors = colors, onRetry = onRetry)
    }
}

@Composable
internal fun RetryButton(
    colors: DailyFeedColors,
    onRetry: () -> Unit,
) {
    val shape = thulurDefaultShape()

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .background(colors.accent, shape)
            .clickable(onClick = onRetry)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        BasicText(
            text = "Retry",
            style = ThulurTheme.Typography.labelLarge.copy(color = colors.onAccent),
        )
    }
}

@Composable
@ReadOnlyComposable
internal fun dailyFeedColors(): DailyFeedColors {
    val colors = ThulurTheme.Colors

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> DailyFeedColors(
            surface = colors.slate.s50,
            surfaceContainer = colors.slate.s100,
            onSurface = colors.slate.s900,
            onSurfaceVariant = colors.slate.s700,
            outline = colors.slate.s300,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
        )

        ThemeMode.Dark -> DailyFeedColors(
            surface = colors.slate.s950,
            surfaceContainer = colors.slate.s900,
            onSurface = colors.slate.s50,
            onSurfaceVariant = colors.slate.s300,
            outline = colors.slate.s700,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
        )
    }
}
