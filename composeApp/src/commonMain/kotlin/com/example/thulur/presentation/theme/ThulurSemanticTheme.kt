package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Immutable
data class ThulurButtonSemanticColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Immutable
data class ThulurButtonStateSemanticColors(
    val rest: ThulurButtonSemanticColors,
    val hovered: ThulurButtonSemanticColors,
    val pressed: ThulurButtonSemanticColors,
    val disabled: ThulurButtonSemanticColors,
)
@Immutable
data class ThulurChatScreenSemanticColors(
    // Background of the entire chat content area
    val contentBackground: Color,
    // Background of the user's message bubble
    val userBubbleContainer: Color,
    // Text color inside the user's message bubble
    val userBubbleContent: Color,
    // Background of the AI's message bubble
    val aiBubbleContainer: Color,
    // Text color inside the AI's message bubble
    val aiBubbleContent: Color,
    // Background of the icon placeholder square
    // Input field colors
    val inputField: ThulurTextFieldStateSemanticColors,
    // Send button colors
    val sendButton: ThulurButtonStateSemanticColors,
    // Label above the thread list
    val chatsLabelColor: Color,
)

@Composable
@ReadOnlyComposable
fun rememberThulurChatScreenSemanticColors(): ThulurChatScreenSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurChatScreenSemanticColors(
            contentBackground = slate.s50,
            userBubbleContainer = primary.s500,
            userBubbleContent = slate.s50,
            aiBubbleContainer = slate.s100,
            aiBubbleContent = slate.s900,
            chatsLabelColor = slate.s700,
            inputField = ThulurTextFieldStateSemanticColors(
                rest = ThulurTextFieldSemanticColors(
                    containerColor = slate.s100,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s100,
                ),
                focused = ThulurTextFieldSemanticColors(
                    containerColor = slate.s100,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s500,
                ),
                error = ThulurTextFieldSemanticColors(
                    containerColor = slate.s100,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s500,
                ),
                disabled = ThulurTextFieldSemanticColors(
                    containerColor = slate.s100,
                    contentColor = slate.s500,
                    placeholderColor = slate.s300,
                    borderColor = slate.s300A10,
                ),
            ),
            sendButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
        )

        ThemeMode.Dark -> ThulurChatScreenSemanticColors(
            contentBackground = slate.s950,
            userBubbleContainer = primary.s500,
            userBubbleContent = slate.s950,
            aiBubbleContainer = slate.s900,
            aiBubbleContent = slate.s100,
            chatsLabelColor = slate.s300,
            inputField = ThulurTextFieldStateSemanticColors(
                rest = ThulurTextFieldSemanticColors(
                    containerColor = slate.s900,
                    contentColor = slate.s300,
                    placeholderColor = slate.s500,
                    borderColor = slate.s900,
                ),
                focused = ThulurTextFieldSemanticColors(
                    containerColor = slate.s900,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = slate.s300,
                ),
                error = ThulurTextFieldSemanticColors(
                    containerColor = slate.s900,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = slate.s300,
                ),
                disabled = ThulurTextFieldSemanticColors(
                    containerColor = slate.s900,
                    contentColor = slate.s500,
                    placeholderColor = slate.s700,
                    borderColor = slate.s700A10,
                ),
            ),
            sendButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
            ),
        )
    }
}
@Immutable
data class ThulurButtonTooltipSemanticColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Immutable
data class ThulurTextFieldSemanticColors(
    val containerColor: Color,
    val contentColor: Color,
    val placeholderColor: Color,
    val borderColor: Color,
)

@Immutable
data class ThulurTextFieldStateSemanticColors(
    val rest: ThulurTextFieldSemanticColors,
    val focused: ThulurTextFieldSemanticColors,
    val error: ThulurTextFieldSemanticColors,
    val disabled: ThulurTextFieldSemanticColors,
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
    val backButton: ThulurButtonStateSemanticColors,
    val forwardButton: ThulurButtonStateSemanticColors,
    val settingsButton: ThulurButtonStateSemanticColors,
    val titleColor: Color,
    val brandColor: Color,
)

@Immutable
data class ThulurThreadItemSemanticColors(
    val titleColor: Color,
    val summaryColor: Color,
    val showWholeSubjectButton: ThulurButtonStateSemanticColors,
    val toggleArticlesButton: ThulurButtonStateSemanticColors,
    val moreArticlesButton: ThulurButtonStateSemanticColors,
)

@Immutable
data class ThulurArticleItemVariantSemanticColors(
    val textColor: Color,
    val imageContainerColor: Color,
    val imageOverlayColor: Color = Color.Transparent,
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
    val imageLoadingIndicatorColor: Color,
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
data class ThulurSnackBarSemanticColors(
    val containerColor: Color,
    val defaultTextColor: Color,
    val errorTextColor: Color,
    val successTextColor: Color,
    val actionButton: ThulurButtonStateSemanticColors,
    val dismissButton: ThulurButtonStateSemanticColors,
)

@Immutable
data class ThulurAuthScreenSemanticColors(
    val screenBackground: Color,
    val cardContainer: Color,
    val cardBorder: Color,
    val iconColor: Color,
    val titleColor: Color,
    val subtitleColor: Color,
    val emailField: ThulurTextFieldStateSemanticColors,
    val continueButton: ThulurButtonStateSemanticColors,
    val troubleLinkButton: ThulurButtonStateSemanticColors,
    val errorColor: Color,
)

@Immutable
data class ThulurRootLoadingScreenSemanticColors(
    val screenBackground: Color,
    val indicatorColor: Color,
)

@Immutable
data class ThulurSettingsSectionItemSemanticColors(
    val rest: ThulurButtonSemanticColors,
    val hovered: ThulurButtonSemanticColors,
    val pressed: ThulurButtonSemanticColors,
    val selected: ThulurButtonSemanticColors,
    val disabled: ThulurButtonSemanticColors,
)

@Immutable
data class ThulurSettingsTimeSelectorSemanticColors(
    val segmentContainerColor: Color,
    val segmentHoveredContainerColor: Color,
    val segmentDisabledContainerColor: Color,
    val segmentContentColor: Color,
    val segmentDisabledContentColor: Color,
    val dividerColor: Color,
    val arrowRestColor: Color,
    val arrowHoveredColor: Color,
    val arrowDisabledColor: Color,
)

@Immutable
data class ThulurSettingsScreenSemanticColors(
    val screenBackground: Color,
    val railColor: Color,
    val sectionTitleColor: Color,
    val subsectionTitleColor: Color,
    val bodyColor: Color,
    val bodyMutedColor: Color,
    val metaColor: Color,
    val dividerColor: Color,
    val errorColor: Color,
    val inputField: ThulurTextFieldStateSemanticColors,
    val inlineActionButton: ThulurButtonStateSemanticColors,
    val terminateButton: ThulurButtonStateSemanticColors,
    val dropdownMenuContainerColor: Color,
    val dropdownMenuContentColor: Color,
    val switchCheckedThumbColor: Color,
    val switchCheckedTrackColor: Color,
    val switchUncheckedThumbColor: Color,
    val switchUncheckedTrackColor: Color,
    val switchDisabledThumbColor: Color,
    val switchDisabledTrackColor: Color,
)

@Immutable
data class ThulurSemanticColors(
    val buttonTooltip: ThulurButtonTooltipSemanticColors,
    val appBar: ThulurAppBarSemanticColors,
    val topicsSwitch: ThulurSegmentedSwitchSemanticColors,
    val threadItem: ThulurThreadItemSemanticColors,
    val articleItem: ThulurArticleItemSemanticColors,
    val dateTime: ThulurDateTimeSemanticColors,
    val chatFab: ThulurChatFabSemanticColors,
    val snackBar: ThulurSnackBarSemanticColors,
    val authScreen: ThulurAuthScreenSemanticColors,
    val rootLoadingScreen: ThulurRootLoadingScreenSemanticColors,
    val settingsScreen: ThulurSettingsScreenSemanticColors,
    val settingsSectionItem: ThulurSettingsSectionItemSemanticColors,
    val settingsTimeSelector: ThulurSettingsTimeSelectorSemanticColors,
    val chatScreen: ThulurChatScreenSemanticColors,
)

@Immutable
data class ThulurSemanticTypography(
    val buttonTooltip: TextStyle,
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
    val snackBarMessage: TextStyle,
    val authTitle: TextStyle,
    val authSubtitle: TextStyle,
    val authInputText: TextStyle,
    val authInputPlaceholder: TextStyle,
    val authPrimaryAction: TextStyle,
    val authTroubleLink: TextStyle,
    val settingsSelectorLabel: TextStyle,
    val settingsSectionTitle: TextStyle,
    val settingsSubsectionTitle: TextStyle,
    val settingsBody: TextStyle,
    val settingsMeta: TextStyle,
    val settingsAction: TextStyle,
    val settingsTimeValue: TextStyle,
)
@Composable
@ReadOnlyComposable
fun rememberThulurButtonTooltipSemanticColors(): ThulurButtonTooltipSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurButtonTooltipSemanticColors(
            containerColor = slate.s950,
            contentColor = slate.s50,
        )

        ThemeMode.Dark -> ThulurButtonTooltipSemanticColors(
            containerColor = slate.s50,
            contentColor = slate.s950,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurAppBarSemanticColors(): ThulurAppBarSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurAppBarSemanticColors(
            containerColor = slate.s100,
            backAreaColor = slate.s300,
            backButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s950,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            forwardButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            settingsButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s950,
                ),
                disabled = ThulurButtonSemanticColors(
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
            backButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            forwardButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            settingsButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
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
            showWholeSubjectButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = primary.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = primary.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s300,
                ),
            ),
            toggleArticlesButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = slate.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = slate.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            moreArticlesButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
        )

        ThemeMode.Dark -> ThulurThreadItemSemanticColors(
            titleColor = slate.s50,
            summaryColor = slate.s300,
            showWholeSubjectButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = primary.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = primary.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s300,
                ),
            ),
            toggleArticlesButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = slate.s500,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = slate.s700,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            moreArticlesButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
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
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurArticleItemSemanticColors(
            linkColor = slate.s900,
            imageLoadingIndicatorColor = primary.s500,
            interaction = ThulurArticleItemInteractionSemanticColors(
                restContainerColor = Color.Transparent,
                hoveredContainerColor = slate.s100,
                pressedContainerColor = slate.s300,
            ),
            trash = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s500,
                imageContainerColor = slate.s100,
                imageOverlayColor = slate.s100.copy(alpha = 0.75f),
                imageLabelColor = slate.s500,
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
            imageLoadingIndicatorColor = primary.s500,
            interaction = ThulurArticleItemInteractionSemanticColors(
                restContainerColor = Color.Transparent,
                hoveredContainerColor = slate.s900,
                pressedContainerColor = slate.s700,
            ),
            trash = ThulurArticleItemVariantSemanticColors(
                textColor = slate.s500,
                imageContainerColor = slate.s700,
                imageOverlayColor = slate.s900.copy(alpha = 0.75f),
                imageLabelColor = slate.s500,
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
fun rememberThulurAuthScreenSemanticColors(): ThulurAuthScreenSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary
    val error = ThulurTheme.Colors.error

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurAuthScreenSemanticColors(
            screenBackground = slate.s50,
            cardContainer = slate.s100,
            cardBorder = slate.s700A10,
            iconColor = slate.s950,
            titleColor = slate.s950,
            subtitleColor = slate.s500,
            emailField = ThulurTextFieldStateSemanticColors(
                rest = ThulurTextFieldSemanticColors(
                    containerColor = slate.s50,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s700A10,
                ),
                focused = ThulurTextFieldSemanticColors(
                    containerColor = slate.s50,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s700,
                ),
                error = ThulurTextFieldSemanticColors(
                    containerColor = slate.s50,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = error.s500,
                ),
                disabled = ThulurTextFieldSemanticColors(
                    containerColor = slate.s100,
                    contentColor = slate.s500,
                    placeholderColor = slate.s300,
                    borderColor = slate.s300A10,
                ),
            ),
            continueButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = primary.s500,
                    contentColor = slate.s50,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = primary.s700,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = primary.s900,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = primary.s100,
                    contentColor = slate.s500,
                ),
            ),
            troubleLinkButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s900,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s950,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
            ),
            errorColor = error.s700,
        )

        ThemeMode.Dark -> ThulurAuthScreenSemanticColors(
            screenBackground = slate.s950,
            cardContainer = slate.s900,
            cardBorder = slate.s50A30,
            iconColor = slate.s50,
            titleColor = slate.s50,
            subtitleColor = slate.s300,
            emailField = ThulurTextFieldStateSemanticColors(
                rest = ThulurTextFieldSemanticColors(
                    containerColor = slate.s950,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = slate.s50A30,
                ),
                focused = ThulurTextFieldSemanticColors(
                    containerColor = slate.s950,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = slate.s300,
                ),
                error = ThulurTextFieldSemanticColors(
                    containerColor = slate.s950,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = error.s300,
                ),
                disabled = ThulurTextFieldSemanticColors(
                    containerColor = slate.s900,
                    contentColor = slate.s500,
                    placeholderColor = slate.s700,
                    borderColor = slate.s300A10,
                ),
            ),
            continueButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = primary.s500,
                    contentColor = slate.s50,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = primary.s700,
                    contentColor = slate.s50,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = primary.s900,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = primary.s900,
                    contentColor = slate.s500,
                ),
            ),
            troubleLinkButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s300,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s100,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
            ),
            errorColor = error.s300,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurRootLoadingScreenSemanticColors(): ThulurRootLoadingScreenSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurRootLoadingScreenSemanticColors(
            screenBackground = slate.s50,
            indicatorColor = primary.s500,
        )

        ThemeMode.Dark -> ThulurRootLoadingScreenSemanticColors(
            screenBackground = slate.s950,
            indicatorColor = primary.s500,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSettingsSectionItemSemanticColors(): ThulurSettingsSectionItemSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurSettingsSectionItemSemanticColors(
            rest = ThulurButtonSemanticColors(
                containerColor = Color.Transparent,
                contentColor = slate.s700,
            ),
            hovered = ThulurButtonSemanticColors(
                containerColor = slate.s300,
                contentColor = slate.s900,
            ),
            pressed = ThulurButtonSemanticColors(
                containerColor = slate.s500,
                contentColor = slate.s950,
            ),
            selected = ThulurButtonSemanticColors(
                containerColor = primary.s500,
                contentColor = slate.s50,
            ),
            disabled = ThulurButtonSemanticColors(
                containerColor = Color.Transparent,
                contentColor = slate.s300,
            ),
        )

        ThemeMode.Dark -> ThulurSettingsSectionItemSemanticColors(
            rest = ThulurButtonSemanticColors(
                containerColor = Color.Transparent,
                contentColor = slate.s300,
            ),
            hovered = ThulurButtonSemanticColors(
                containerColor = slate.s700,
                contentColor = slate.s100,
            ),
            pressed = ThulurButtonSemanticColors(
                containerColor = slate.s500,
                contentColor = slate.s50,
            ),
            selected = ThulurButtonSemanticColors(
                containerColor = primary.s500,
                contentColor = slate.s50,
            ),
            disabled = ThulurButtonSemanticColors(
                containerColor = Color.Transparent,
                contentColor = slate.s700,
            ),
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSettingsTimeSelectorSemanticColors(): ThulurSettingsTimeSelectorSemanticColors {
    val slate = ThulurTheme.Colors.slate

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurSettingsTimeSelectorSemanticColors(
            segmentContainerColor = slate.s300,
            segmentHoveredContainerColor = slate.s300,
            segmentDisabledContainerColor = slate.s100,
            segmentContentColor = slate.s950,
            segmentDisabledContentColor = slate.s500,
            dividerColor = slate.s950,
            arrowRestColor = slate.s700,
            arrowHoveredColor = slate.s950,
            arrowDisabledColor = slate.s300,
        )

        ThemeMode.Dark -> ThulurSettingsTimeSelectorSemanticColors(
            segmentContainerColor = slate.s700,
            segmentHoveredContainerColor = slate.s700,
            segmentDisabledContainerColor = slate.s900,
            segmentContentColor = slate.s50,
            segmentDisabledContentColor = slate.s500,
            dividerColor = slate.s50,
            arrowRestColor = slate.s300,
            arrowHoveredColor = slate.s50,
            arrowDisabledColor = slate.s700,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSettingsScreenSemanticColors(): ThulurSettingsScreenSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val primary = ThulurTheme.Colors.primary
    val error = ThulurTheme.Colors.error

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurSettingsScreenSemanticColors(
            screenBackground = slate.s50,
            railColor = slate.s100,
            sectionTitleColor = slate.s950,
            subsectionTitleColor = slate.s950,
            bodyColor = slate.s900,
            bodyMutedColor = slate.s700,
            metaColor = slate.s500,
            dividerColor = slate.s300,
            errorColor = error.s700,
            inputField = ThulurTextFieldStateSemanticColors(
                rest = ThulurTextFieldSemanticColors(
                    containerColor = slate.s50,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s700A10,
                ),
                focused = ThulurTextFieldSemanticColors(
                    containerColor = slate.s50,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = slate.s500,
                ),
                error = ThulurTextFieldSemanticColors(
                    containerColor = slate.s50,
                    contentColor = slate.s700,
                    placeholderColor = slate.s500,
                    borderColor = error.s500,
                ),
                disabled = ThulurTextFieldSemanticColors(
                    containerColor = slate.s100,
                    contentColor = slate.s500,
                    placeholderColor = slate.s300,
                    borderColor = slate.s300A10,
                ),
            ),
            inlineActionButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s700,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s900,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s300,
                ),
            ),
            terminateButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = error.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = error.s700,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = error.s900,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = error.s300,
                ),
            ),
            dropdownMenuContainerColor = slate.s100,
            dropdownMenuContentColor = slate.s900,
            switchCheckedThumbColor = slate.s50,
            switchCheckedTrackColor = primary.s500,
            switchUncheckedThumbColor = slate.s50,
            switchUncheckedTrackColor = slate.s300,
            switchDisabledThumbColor = slate.s300,
            switchDisabledTrackColor = slate.s100,
        )

        ThemeMode.Dark -> ThulurSettingsScreenSemanticColors(
            screenBackground = slate.s950,
            railColor = slate.s900,
            sectionTitleColor = slate.s50,
            subsectionTitleColor = slate.s50,
            bodyColor = slate.s100,
            bodyMutedColor = slate.s300,
            metaColor = slate.s500,
            dividerColor = slate.s700,
            errorColor = error.s300,
            inputField = ThulurTextFieldStateSemanticColors(
                rest = ThulurTextFieldSemanticColors(
                    containerColor = slate.s950,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = slate.s50A30,
                ),
                focused = ThulurTextFieldSemanticColors(
                    containerColor = slate.s950,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = slate.s300,
                ),
                error = ThulurTextFieldSemanticColors(
                    containerColor = slate.s950,
                    contentColor = slate.s100,
                    placeholderColor = slate.s500,
                    borderColor = error.s300,
                ),
                disabled = ThulurTextFieldSemanticColors(
                    containerColor = slate.s900,
                    contentColor = slate.s500,
                    placeholderColor = slate.s700,
                    borderColor = slate.s300A10,
                ),
            ),
            inlineActionButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s500,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = primary.s300,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s500,
                ),
            ),
            terminateButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = error.s300,
                ),
                hovered = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = error.s100,
                ),
                pressed = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s50,
                ),
                disabled = ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = slate.s700,
                ),
            ),
            dropdownMenuContainerColor = slate.s900,
            dropdownMenuContentColor = slate.s100,
            switchCheckedThumbColor = slate.s50,
            switchCheckedTrackColor = primary.s500,
            switchUncheckedThumbColor = slate.s50,
            switchUncheckedTrackColor = slate.s500,
            switchDisabledThumbColor = slate.s500,
            switchDisabledTrackColor = slate.s700,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurButtonSemanticColors(
    colorRole: ThulurColorRole,
    enabled: Boolean,
    isHovered: Boolean,
    isPressed: Boolean,
    useContainerStates: Boolean,
    stateColorsOverride: ThulurButtonStateSemanticColors? = null,
): ThulurButtonSemanticColors {
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
                !enabled -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s100,
                )

                isPressed -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s900,
                )

                isHovered -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s700,
                )

                else -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )
            }

            ThemeMode.Dark -> when {
                !enabled -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s900,
                )

                isPressed -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s100,
                )

                isHovered -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s300,
                )

                else -> ThulurButtonSemanticColors(
                    containerColor = Color.Transparent,
                    contentColor = roleScale.s500,
                )
            }
        }
    }

    return when {
        !enabled -> ThulurButtonSemanticColors(
            containerColor = Color.Transparent,
            contentColor = roleScale.s300,
        )

        isPressed -> ThulurButtonSemanticColors(
            containerColor = roleScale.s700,
            contentColor = slate.s50,
        )

        isHovered -> ThulurButtonSemanticColors(
            containerColor = roleScale.s500,
            contentColor = slate.s50,
        )

        else -> ThulurButtonSemanticColors(
            containerColor = Color.Transparent,
            contentColor = roleScale.s500,
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurTextFieldSemanticColors(
    enabled: Boolean,
    isFocused: Boolean,
    isError: Boolean,
    stateColorsOverride: ThulurTextFieldStateSemanticColors,
): ThulurTextFieldSemanticColors = when {
    !enabled -> stateColorsOverride.disabled
    isError -> stateColorsOverride.error
    isFocused -> stateColorsOverride.focused
    else -> stateColorsOverride.rest
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
    buttonTooltip = ThulurTheme.Typography.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.thulurSp(),
        lineHeight = 12.thulurSp(),
    ),
    appBarBackLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.ExtraLight,
        fontSize = 24.thulurSp(),
        lineHeight = 32.thulurSp(),
    ),
    appBarTitle = ThulurTheme.Typography.headlineLarge.copy(
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
    snackBarMessage = ThulurTheme.Typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.thulurSp(),
        lineHeight = 14.thulurSp(),
    ),
    authTitle = ThulurTheme.Typography.displaySmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 36.thulurSp(),
        lineHeight = 44.thulurSp(),
    ),
    authSubtitle = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.thulurSp(),
        lineHeight = 24.thulurSp(),
    ),
    authInputText = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.thulurSp(),
        lineHeight = 24.thulurSp(),
    ),
    authInputPlaceholder = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.thulurSp(),
        lineHeight = 24.thulurSp(),
    ),
    authPrimaryAction = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.thulurSp(),
        lineHeight = 20.thulurSp(),
    ),
    authTroubleLink = ThulurTheme.Typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.thulurSp(),
        lineHeight = 20.thulurSp(),
        textDecoration = TextDecoration.Underline,
    ),
    settingsSelectorLabel = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 18.thulurSp(),
        lineHeight = 18.thulurSp(),
    ),
    settingsSectionTitle = ThulurTheme.Typography.headlineLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.thulurSp(),
        lineHeight = 40.thulurSp(),
    ),
    settingsSubsectionTitle = ThulurTheme.Typography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.thulurSp(),
        lineHeight = 30.thulurSp(),
    ),
    settingsBody = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.thulurSp(),
        lineHeight = 24.thulurSp(),
    ),
    settingsMeta = ThulurTheme.Typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.thulurSp(),
        lineHeight = 20.thulurSp(),
    ),
    settingsAction = ThulurTheme.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.thulurSp(),
        lineHeight = 20.thulurSp(),
    ),
    settingsTimeValue = ThulurTheme.Typography.headlineLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 20.thulurSp(),
        lineHeight = 20.thulurSp(),
    ),
)

@Composable
@ReadOnlyComposable
fun rememberThulurSnackBarSemanticColors(): ThulurSnackBarSemanticColors {
    val slate = ThulurTheme.Colors.slate
    val error = ThulurTheme.Colors.error
    val success = ThulurTheme.Colors.success
    val primary = ThulurTheme.Colors.primary

    return when (ThulurTheme.Mode) {
        ThemeMode.Light -> ThulurSnackBarSemanticColors(
            containerColor = slate.s300,
            defaultTextColor = slate.s700,
            errorTextColor = error.s700,
            successTextColor = success.s700,
            actionButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(Color.Transparent, primary.s500),
                hovered = ThulurButtonSemanticColors(Color.Transparent, primary.s700),
                pressed = ThulurButtonSemanticColors(Color.Transparent, primary.s900),
                disabled = ThulurButtonSemanticColors(Color.Transparent, primary.s300),
            ),
            dismissButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(Color.Transparent, slate.s700),
                hovered = ThulurButtonSemanticColors(Color.Transparent, slate.s900),
                pressed = ThulurButtonSemanticColors(Color.Transparent, slate.s950),
                disabled = ThulurButtonSemanticColors(Color.Transparent, slate.s500),
            ),
        )
        ThemeMode.Dark -> ThulurSnackBarSemanticColors(
            containerColor = slate.s700,
            defaultTextColor = slate.s300,
            errorTextColor = error.s300,
            successTextColor = success.s300,
            actionButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(Color.Transparent, primary.s300),
                hovered = ThulurButtonSemanticColors(Color.Transparent, primary.s100),
                pressed = ThulurButtonSemanticColors(Color.Transparent, primary.s50),
                disabled = ThulurButtonSemanticColors(Color.Transparent, primary.s700),
            ),
            dismissButton = ThulurButtonStateSemanticColors(
                rest = ThulurButtonSemanticColors(Color.Transparent, slate.s300),
                hovered = ThulurButtonSemanticColors(Color.Transparent, slate.s100),
                pressed = ThulurButtonSemanticColors(Color.Transparent, slate.s50),
                disabled = ThulurButtonSemanticColors(Color.Transparent, slate.s500),
            ),
        )
    }
}

@Composable
@ReadOnlyComposable
fun rememberThulurSemanticColors(): ThulurSemanticColors = ThulurSemanticColors(
    buttonTooltip = rememberThulurButtonTooltipSemanticColors(),
    appBar = rememberThulurAppBarSemanticColors(),
    topicsSwitch = rememberTopicsSwitchSemanticColors(),
    threadItem = rememberThulurThreadItemSemanticColors(),
    articleItem = rememberThulurArticleItemSemanticColors(),
    dateTime = rememberThulurDateTimeSemanticColors(),
    chatFab = rememberThulurChatFabSemanticColors(),
    snackBar = rememberThulurSnackBarSemanticColors(),
    authScreen = rememberThulurAuthScreenSemanticColors(),
    rootLoadingScreen = rememberThulurRootLoadingScreenSemanticColors(),
    settingsScreen = rememberThulurSettingsScreenSemanticColors(),
    settingsSectionItem = rememberThulurSettingsSectionItemSemanticColors(),
    settingsTimeSelector = rememberThulurSettingsTimeSelectorSemanticColors(),
    chatScreen = rememberThulurChatScreenSemanticColors(),
)
