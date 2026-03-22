package com.example.thulur_front

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import com.example.thulur_front.theme.ThemeMode
import com.example.thulur_front.theme.ThulurTheme

import thulur_front.composeapp.generated.resources.Res
import thulur_front.composeapp.generated.resources.compose_multiplatform

private data class AppColorAliases(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val accent: Color,
    val onAccent: Color,
)

@Composable
fun App(mode: ThemeMode = ThemeMode.Light) {
    ThulurTheme(mode = mode) {
        var showContent by remember { mutableStateOf(false) }
        val colors = rememberAppColorAliases()

        Column(
            modifier = Modifier
                .background(colors.surface)
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.accent)
                    .clickable { showContent = !showContent }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                BasicText(
                    text = "Click me!",
                    style = ThulurTheme.Typography.labelLarge.copy(color = colors.onAccent),
                )
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surfaceContainer)
                        .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surfaceContainerHigh)
                            .padding(12.dp),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.compose_multiplatform),
                            contentDescription = null,
                        )
                    }
                    BasicText(
                        text = "Compose",
                        style = ThulurTheme.Typography.headlineMedium.copy(color = colors.onSurface),
                    )
                    BasicText(
                        text = greeting,
                        style = ThulurTheme.Typography.bodyLarge.copy(color = colors.onSurfaceVariant),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberAppColorAliases(): AppColorAliases {
    val colors = ThulurTheme.Colors

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> AppColorAliases(
            surface = colors.slate.s50,
            surfaceContainer = colors.slate.s100,
            surfaceContainerHigh = colors.primary.s300A08,
            onSurface = colors.slate.s900,
            onSurfaceVariant = colors.slate.s700,
            outline = colors.slate.s300,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
        )

        ThemeMode.Dark -> AppColorAliases(
            surface = colors.slate.s950,
            surfaceContainer = colors.slate.s900,
            surfaceContainerHigh = colors.slate.s300A08,
            onSurface = colors.slate.s50,
            onSurfaceVariant = colors.slate.s300,
            outline = colors.slate.s700,
            accent = colors.primary.s500,
            onAccent = colors.slate.s50,
        )
    }
}

@Preview
@Composable
private fun AppLightPreview() {
    App(mode = ThemeMode.Light)
}

@Preview
@Composable
private fun AppDarkPreview() {
    App(mode = ThemeMode.Dark)
}
