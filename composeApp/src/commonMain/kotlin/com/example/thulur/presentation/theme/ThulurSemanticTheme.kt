package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class ThulurTextButtonSemanticColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Immutable
data class ThulurTextButtonStateSemanticColors(
    val rest: ThulurTextButtonSemanticColors,
    val hovered: ThulurTextButtonSemanticColors,
    val pressed: ThulurTextButtonSemanticColors,
    val disabled: ThulurTextButtonSemanticColors,
)

@Immutable
data class ThulurSegmentedSwitchSemanticColors(
    val containerColor: Color,
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val disabledContentColor: Color,
)

@Immutable
data class ThulurAppBarSemanticColors(
    val containerColor: Color,
    val backAreaColor: Color,
    val backButton: ThulurTextButtonStateSemanticColors,
    val forwardButton: ThulurTextButtonStateSemanticColors,
    val settingsButton: ThulurTextButtonStateSemanticColors,
    val titleColor: Color,
    val brandColor: Color,
)

@Immutable
data class ThulurThreadItemSemanticColors(
    val titleColor: Color,
    val summaryColor: Color,
    val showWholeSubjectButton: ThulurTextButtonStateSemanticColors,
    val toggleArticlesButton: ThulurTextButtonStateSemanticColors,
    val moreArticlesButton: ThulurTextButtonStateSemanticColors,
)

@Immutable
data class ThulurArticleItemVariantSemanticColors(
    val textColor: Color,
    val imageContainerColor: Color,
    val imageLabelColor: Color,
    val outlineColor: Color = Color.Transparent,
)

@Immutable
data class ThulurArticleItemInteractionSemanticColors(
    val restContainerColor: Color,
    val hoveredContainerColor: Color,
    val pressedContainerColor: Color,
)

@Immutable
data class ThulurArticleItemSemanticColors(
    val linkColor: Color,
    val interaction: ThulurArticleItemInteractionSemanticColors,
    val trash: ThulurArticleItemVariantSemanticColors,
    val default: ThulurArticleItemVariantSemanticColors,
    val important: ThulurArticleItemVariantSemanticColors,
)

@Immutable
data class ThulurDateTimeSemanticColors(
    val dateColor: Color,
    val timeColor: Color,
)

@Immutable
data class ThulurChatFabSemanticColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Immutable
data class ThulurSemanticColors(
    val appBar: ThulurAppBarSemanticColors,
    val topicsSwitch: ThulurSegmentedSwitchSemanticColors,
    val threadItem: ThulurThreadItemSemanticColors,
    val articleItem: ThulurArticleItemSemanticColors,
    val dateTime: ThulurDateTimeSemanticColors,
    val chatFab: ThulurChatFabSemanticColors,
)

@Immutable
data class ThulurSemanticTypography(
    val appBarBackLabel: TextStyle,
    val appBarTitle: TextStyle,
    val appBarBrand: TextStyle,
    val topicsSwitchLabel: TextStyle,
    val threadItemTitle: TextStyle,
    val threadItemSummary: TextStyle,
    val threadItemControl: TextStyle,
    val articleItemTitle: TextStyle,
    val articleItemImportantTitle: TextStyle,
    val articleItemSummary: TextStyle,
    val articleItemImportantSummary: TextStyle,
    val articleItemImageLabel: TextStyle,
    val articleItemLink: TextStyle,
    val dateTimeDate: TextStyle,
    val dateTimeTime: TextStyle,
    val chatFabLabel: TextStyle,
)

@Composable
@ReadOnlyComposable
fun rememberThulurAppBarSemanticColors(): ThulurAppBarSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurAppBarSemanticColors(
            containerColor = slate.s100,
            backAreaColor = slate.s300,
            backButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s950,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            forwardButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            settingsButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s950,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            titleColor = slate.s950,
            brandColor = slate.s950,
        )

        ThemeMode.Dark -> ThulurAppBarSemanticColors(
            containerColor = slate.s900,
            backAreaColor = slate.s700,
            backButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            forwardButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            settingsButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            titleColor = slate.s50,
            brandColor = slate.s50,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurThreadItemSemanticColors(): ThulurThreadItemSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurThreadItemSemanticColors(
            titleColor = slate.s950,
            summaryColor = slate.s700,
            showWholeSubjectButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s500,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = primary.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = primary.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s300,
                ),
            ),
            toggleArticlesButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = slate.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = slate.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            moreArticlesButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s950,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
        )

        ThemeMode.Dark -> ThulurThreadItemSemanticColors(
            titleColor = slate.s50,
            summaryColor = slate.s300,
            showWholeSubjectButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s500,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = primary.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = primary.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s300,
                ),
            ),
            toggleArticlesButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = slate.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = slate.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            moreArticlesButton = ThulurTextButtonStateSemanticColors(
                rest = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                hovered = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                pressed = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurArticleItemSemanticColors(): ThulurArticleItemSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurArticleItemSemanticColors(
            linkColor = slate.s900,
            interaction = ThulurArticleItemInteractionSemanticColors(
                restContainerColor = Color.Transparent,
                hoveredContainerColor = slate.s100,
                pressedContainerColor = slate.s300,
            ),
            trash = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s500,
                imageContainerColor = slate.s100,
                imageLabelColor = slate.s300,
            ),
            default = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s900,
                imageContainerColor = slate.s300,
                imageLabelColor = slate.s500,
            ),
            important = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s950,
                imageContainerColor = slate.s300,
                imageLabelColor = slate.s500,
                outlineColor = slate.s950A30,
            ),
        )

        ThemeMode.Dark -> ThulurArticleItemSemanticColors(
            linkColor = slate.s100,
            interaction = ThulurArticleItemInteractionSemanticColors(
                restContainerColor = Color.Transparent,
                hoveredContainerColor = slate.s900,
                pressedContainerColor = slate.s700,
            ),
            trash = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s500,
                imageContainerColor = slate.s900,
                imageLabelColor = slate.s700,
            ),
            default = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s100,
                imageContainerColor = slate.s700,
                imageLabelColor = slate.s500,
            ),
            important = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s50,
                imageContainerColor = slate.s700,
                imageLabelColor = slate.s500,
                outlineColor = slate.s50A30,
            ),
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberTopicsSwitchSemanticColors(): ThulurSegmentedSwitchSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurSegmentedSwitchSemanticColors(
            containerColor = slate.s300,
            selectedContainerColor = primary.s500,
            selectedContentColor = slate.s50,
            unselectedContentColor = slate.s700,
            disabledContentColor = primary.s300,
        )

        ThemeMode.Dark -> ThulurSegmentedSwitchSemanticColors(
            containerColor = slate.s700,
            selectedContainerColor = primary.s500,
            selectedContentColor = slate.s50,
            unselectedContentColor = slate.s300,
            disabledContentColor = primary.s300,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurDateTimeSemanticColors(): ThulurDateTimeSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurDateTimeSemanticColors(
            dateColor = slate.s950,
            timeColor = slate.s950,
        )

        ThemeMode.Dark -> ThulurDateTimeSemanticColors(
            dateColor = slate.s50,
            timeColor = slate.s50,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurChatFabSemanticColors(): ThulurChatFabSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurChatFabSemanticColors(
            containerColor = primary.s500,
            contentColor = slate.s50,
        )

        ThemeMode.Dark -> ThulurChatFabSemanticColors(
            containerColor = primary.s500,
            contentColor = slate.s50,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurTextButtonSemanticColors(
    colorRole: ThulurColorRole,
    enabled: Boolean,
    isHovered: Boolean,
    isPressed: Boolean,
    useContainerStates: Boolean,
    stateColorsOverride: ThulurTextButtonStateSemanticColors? = null,
): ThulurTextButtonSemanticColors {
    val roleScale = rememberThulurShadeScale(colorRole)
    val slate = ThulurTheme.Colors.slate

    stateColorsOverride?.let { override ->
        return when {
            !enabled -> override.disabled
            isPressed -> override.pressed
            isHovered -> override.hovered
            else -> override.rest
        }
    }

    if (!useContainerStates) {
        return when (ThulurTheme.Mode) {
            ThemeMode.Light -> when {
                !enabled -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s300,
                )

                isPressed -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s900,
                )

                isHovered -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s700,
                )

                else -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )
            }

            ThemeMode.Dark -> when {
                !enabled -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )

                isPressed -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s100,
                )

                isHovered -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s300,
                )

                else -> ThulurTextButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )
            }
        }
    }

    return when {
        !enabled -> ThulurTextButtonSemanticColors(
            containerColor = Color.Transparent,
            contentColor = roleScale.s300,
        )

        isPressed -> ThulurTextButtonSemanticColors(
            containerColor = roleScale.s700,
            contentColor = slate.s50,
        )

        isHovered -> ThulurTextButtonSemanticColors(
            containerColor = roleScale.s500,
            contentColor = slate.s50,
        )

        else -> ThulurTextButtonSemanticColors(
            containerColor = Color.Transparent,
            contentColor = roleScale.s500,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSegmentedSwitchSemanticColors(
    colorRole: ThulurColorRole,
    enabled: Boolean,
    colorsOverride: ThulurSegmentedSwitchSemanticColors? = null,
): ThulurSegmentedSwitchSemanticColors {
    colorsOverride?.let { override ->
        return if (enabled) {
            override
        } else {
            override.copy(
                selectedContainerColor = override.disabledContentColor,
                unselectedContentColor = override.disabledContentColor,
                disabledContentColor = override.disabledContentColor,
            )
        }
    }

    val slate = ThulurTheme.Colors.slate
    val selectedScale = rememberThulurShadeScale(colorRole)

    val containerColor = when (ThulurTheme.Mode) {
        ThemeMode.Light -> slate.s300
        ThemeMode.Dark -> slate.s700
    }
    val unselectedContentColor = when (ThulurTheme.Mode) {
        ThemeMode.Light -> slate.s700
        ThemeMode.Dark -> slate.s300
    }

    return if (enabled) {
        ThulurSegmentedSwitchSemanticColors(
            containerColor = containerColor,
            selectedContainerColor = selectedScale.s500,
            selectedContentColor = slate.s50,
            unselectedContentColor = unselectedContentColor,
            disabledContentColor = selectedScale.s300,
        )
    } else {
        ThulurSegmentedSwitchSemanticColors(
            containerColor = containerColor,
            selectedContainerColor = selectedScale.s300,
            selectedContentColor = slate.s50,
            unselectedContentColor = selectedScale.s300,
            disabledContentColor = selectedScale.s300,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSemanticTypography(): ThulurSemanticTypography = ThulurSemanticTypography(
    appBarBackLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.ExtraLight,
        fontSize = 24.thulurSp(),
        lineHeight = 32.thulurSp(),
    ),
    appBarTitle = ThulurTheme.Typography.headlineLarge.copy(
        fontFamily = ThulurTheme.Typography.bodyLarge.fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.thulurSp(),
        lineHeight = 42.thulurSp(),
    ),
    appBarBrand = ThulurTheme.Typography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 32.thulurSp(),
        lineHeight = 40.thulurSp(),
    ),
    topicsSwitchLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 18.thulurSp(),
        lineHeight = 18.thulurSp(),
    ),
    threadItemTitle = ThulurTheme.Typography.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.thulurSp(),
        lineHeight = 34.thulurSp(),
    ),
    threadItemSummary = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Light,
        fontSize = 18.thulurSp(),
        lineHeight = 26.thulurSp(),
    ),
    threadItemControl = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 18.thulurSp(),
        lineHeight = 18.thulurSp(),
    ),
    articleItemTitle = ThulurTheme.Typography.titleLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.thulurSp(),
        lineHeight = 16.thulurSp(),
    ),
    articleItemImportantTitle = ThulurTheme.Typography.titleLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 20.thulurSp(),
        lineHeight = 20.thulurSp(),
    ),
    articleItemSummary = ThulurTheme.Typography.bodyMedium.copy(
        fontWeight = FontWeight.ExtraLight,
        fontSize = 14.thulurSp(),
        lineHeight = 14.thulurSp(),
    ),
    articleItemImportantSummary = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Light,
        fontSize = 16.thulurSp(),
        lineHeight = 16.thulurSp(),
    ),
    articleItemImageLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.ExtraLight,
        fontSize = 16.thulurSp(),
        lineHeight = 16.thulurSp(),
    ),
    articleItemLink = ThulurTheme.Typography.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.thulurSp(),
        lineHeight = 12.thulurSp(),
    ),
    dateTimeDate = ThulurTheme.Typography.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.thulurSp(),
        lineHeight = 12.thulurSp(),
    ),
    dateTimeTime = ThulurTheme.Typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.thulurSp(),
        lineHeight = 12.thulurSp(),
    ),
    chatFabLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 20.thulurSp(),
        lineHeight = 20.thulurSp(),
    ),
)

@Composable
@ReadOnlyComposable
fun rememberThulurSemanticColors(): ThulurSemanticColors = ThulurSemanticColors(
    appBar = rememberThulurAppBarSemanticColors(),
    topicsSwitch = rememberTopicsSwitchSemanticColors(),
    threadItem = rememberThulurThreadItemSemanticColors(),
    articleItem = rememberThulurArticleItemSemanticColors(),
    dateTime = rememberThulurDateTimeSemanticColors(),
    chatFab = rememberThulurChatFabSemanticColors(),
)
