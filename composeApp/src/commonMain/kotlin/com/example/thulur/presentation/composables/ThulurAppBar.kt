package com.example.thulur.presentation.composables

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
    modifier: Modifier = Modifier,
    backLabel: String = "Back",
    onBackClick: () -> Unit = {},
    backButton: (@Composable () -> Unit)? = null,
    forwardButton: (@Composable () -> Unit)? = null,
    endPrimaryContent: (@Composable () -> Unit)? = null,
    endSecondaryContent: (@Composable () -> Unit)? = null,
    chatNameContent: (@Composable () -> Unit)? = null,
    brandContent: @Composable () -> Unit = { DefaultThulurAppBarBrand() },
) {
    val colors = ThulurTheme.SemanticColors.appBar
    val typography = ThulurTheme.SemanticTypography
    val appBarHeight = 100.thulurDp()
    val backAreaWidth = 225.thulurDp()
    val backAreaPadding = 10.thulurDp()
    val actionSpacing = 25.thulurDp()
    val contentHorizontalPadding = 30.thulurDp()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(appBarHeight)
            .background(colors.containerColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back area — left rail with back button
        Box(
            modifier = Modifier
                .width(backAreaWidth)
                .fillMaxHeight()
                .background(colors.backAreaColor)
                .padding(backAreaPadding),
            contentAlignment = Alignment.Center,
        ) {
            if (backButton != null) {
                backButton()
            } else {
                DefaultThulurAppBarBackButton(
                    backLabel = backLabel,
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        }

        // Content area — Box allows independent left/center/right alignment
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentHorizontalPadding),
        ) {
            // Left: title + optional forward button
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = title,
                    style = typography.appBarTitle.copy(color = colors.titleColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                forwardButton?.invoke()
            }
            if (chatNameContent != null) {
                Box(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    chatNameContent()
                }
            }

            // Right: end actions + brand
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                endPrimaryContent?.invoke()
                endSecondaryContent?.invoke()
                brandContent()
            }
        }
    }
}

@Composable
private fun DefaultThulurAppBarBackButton(
    backLabel: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.appBar
    val typography = ThulurTheme.SemanticTypography
    val backAreaPadding = 10.thulurDp()
    val backItemSpacing = 10.thulurDp()

    ThulurButton(
        text = backLabel,
        onClick = onBackClick,
        modifier = modifier,
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

@Composable
private fun DefaultThulurAppBarBrand() {
    val colors = ThulurTheme.SemanticColors.appBar
    val typography = ThulurTheme.SemanticTypography

    BasicText(
        text = "Thulur",
        style = typography.appBarBrand.copy(color = colors.brandColor),
    )
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
                forwardButton = {
                    ThulurButton(
                        text = "Tomorrow",
                        onClick = {},
                        colorRole = ThulurColorRole.Slate,
                        useContainerStates = false,
                        stateColorsOverride = ThulurTheme.SemanticColors.appBar.forwardButton,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ArrowCircleRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.thulurDp()),
                            )
                        },
                        textStyle = ThulurTheme.SemanticTypography.appBarBackLabel,
                        contentPadding = PaddingValues(horizontal = 10.thulurDp()),
                        spacing = 10.thulurDp(),
                    )
                },
                endPrimaryContent = {
                    TopicsSwitch(
                        selected = topicsViewMode,
                        onSelect = { topicsViewMode = it },
                    )
                },
                endSecondaryContent = {
                    ThulurButton(
                        onClick = {},
                        colorRole = ThulurColorRole.Slate,
                        useContainerStates = false,
                        stateColorsOverride = ThulurTheme.SemanticColors.appBar.settingsButton,
                        contentPadding = PaddingValues(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(32.thulurDp()),
                            )
                        },
                    )
                },
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
                title = "Article Reader",
                backLabel = "Back",
                onBackClick = {},
                endPrimaryContent = {
                    TopicsSwitch(
                        selected = topicsViewMode,
                        onSelect = { topicsViewMode = it },
                    )
                },
            )
        }
    }
}