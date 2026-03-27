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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThulurAppBar(
    title: String,
    backLabel: String,
    onBackClick: () -> Unit,
    forwardLabel: String? = null,
    onForwardClick: (() -> Unit)? = null,
    topicsViewMode: TopicsViewMode,
    onTopicsViewModeChange: (TopicsViewMode) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.appBar
    val typography = ThulurTheme.SemanticTypography
    val appBarHeight = 100.thulurDp()
    val backAreaWidth = 225.thulurDp()
    val backAreaPadding = 10.thulurDp()
    val backItemSpacing = 10.thulurDp()
    val actionSpacing = 25.thulurDp()
    val contentHorizontalPadding = 30.thulurDp()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(appBarHeight)
            .background(colors.containerColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(backAreaWidth)
                .fillMaxHeight()
                .background(colors.backAreaColor)
                .padding(backAreaPadding),
            contentAlignment = Alignment.Center,
        ) {
            ThulurTextButton(
                text = backLabel,
                onClick = onBackClick,
                modifier = Modifier.fillMaxHeight(),
                colorRole = ThulurColorRole.Slate,
                useContainerStates = false,
                stateColorsOverride = colors.backButton,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ArrowCircleLeft,
                        contentDescription = null,
                        modifier = Modifier.size(24.thulurDp()),
                    )
                },
                textStyle = typography.appBarBackLabel,
                contentPadding = PaddingValues(horizontal = backAreaPadding),
                spacing = backItemSpacing,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentHorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = title,
                    style = typography.appBarTitle.copy(color = colors.titleColor),
                )

                if (forwardLabel != null && onForwardClick != null) {
                    ThulurTextButton(
                        text = forwardLabel,
                        onClick = onForwardClick,
                        colorRole = ThulurColorRole.Slate,
                        useContainerStates = false,
                        stateColorsOverride = colors.forwardButton,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ArrowCircleRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.thulurDp()),
                            )
                        },
                        textStyle = typography.appBarBackLabel,
                        contentPadding = PaddingValues(horizontal = backAreaPadding),
                        spacing = backItemSpacing,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(actionSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val settingsInteractionSource = remember { MutableInteractionSource() }
                    val isSettingsHovered by settingsInteractionSource.collectIsHoveredAsState()
                    val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()
                    val settingsColors = when {
                        isSettingsPressed -> colors.settingsButton.pressed
                        isSettingsHovered -> colors.settingsButton.hovered
                        else -> colors.settingsButton.rest
                    }
                    val settingsTint by animateColorAsState(
                        targetValue = settingsColors.contentColor,
                        label = "thulurAppBarSettingsTint",
                    )

                    TopicsSwitch(
                        selected = topicsViewMode,
                        onSelect = onTopicsViewModeChange,
                    )

                    Box(
                        modifier = Modifier
                            .size(32.thulurDp())
                            .hoverable(interactionSource = settingsInteractionSource)
                            .clickable(
                                interactionSource = settingsInteractionSource,
                                indication = null,
                                onClick = onSettingsClick,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = settingsTint,
                            modifier = Modifier.size(32.thulurDp()),
                        )
                    }
                }

                BasicText(
                    text = "Thulur",
                    style = typography.appBarBrand.copy(color = colors.brandColor),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ThulurAppBarLightPreview() {
    var topicsViewMode by remember { mutableStateOf(TopicsViewMode.TopicsAndArticles) }

    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            ThulurAppBar(
                title = "Today",
                backLabel = "Yesterday",
                onBackClick = {},
                topicsViewMode = topicsViewMode,
                onTopicsViewModeChange = { topicsViewMode = it },
                onSettingsClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun ThulurAppBarDarkCompactPreview() {
    var topicsViewMode by remember { mutableStateOf(TopicsViewMode.TopicsOnly) }

    ProvideThulurDesignScale(scale = ThulurDesignScale(factor = 0.75f)) {
        ThulurTheme(mode = ThemeMode.Dark) {
            ThulurAppBar(
                title = "Today",
                backLabel = "Yesterday",
                onBackClick = {},
                topicsViewMode = topicsViewMode,
                onTopicsViewModeChange = { topicsViewMode = it },
                onSettingsClick = {},
            )
        }
    }
}
