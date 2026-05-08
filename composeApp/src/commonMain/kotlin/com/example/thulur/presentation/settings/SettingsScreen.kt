package com.example.thulur.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurSnackBar
import com.example.thulur.presentation.composables.ThulurSnackBarState
import com.example.thulur.presentation.composables.ThulurTextField
import com.example.thulur.presentation.composables.ThulurTimeSelector
import com.example.thulur.presentation.settings.components.AppSettingItem
import com.example.thulur.presentation.settings.components.SessionItem
import com.example.thulur.presentation.settings.components.SettingsDropdown
import com.example.thulur.presentation.settings.components.SettingsFeedsSection
import com.example.thulur.presentation.settings.components.SettingsSectionSelector
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    sessionInstanceId: Int,
    onBackClick: () -> Unit,
    onThemeApplied: (ThemeMode) -> Unit,
    viewModel: SettingsViewModel = koinViewModel(key = settingsViewModelKey(sessionInstanceId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Propagate theme changes (including optimistic updates and rollbacks) to the root
    // without firing on the first composition.
    var prevTheme by remember { mutableStateOf(uiState.appState.values.theme) }
    val currentTheme = uiState.appState.values.theme
    SideEffect {
        if (prevTheme != currentTheme) {
            onThemeApplied(currentTheme)
            prevTheme = currentTheme
        }
    }

    var snackBarId by remember { mutableLongStateOf(0L) }
    var snackBarMessage by remember { mutableStateOf("") }
    var showSnackBar by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.snackBarEvents.collect { event ->
            snackBarId++
            showSnackBar = true
            when (event) {
                is SettingsSnackBarEvent.Error -> {
                    snackBarMessage = event.message
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onRetryLoad = viewModel::retryLoad,
            onSectionSelected = viewModel::onSectionSelected,
            onTerminateSessionClick = viewModel::onTerminateSessionClick,
            onRetryFeedsLoad = viewModel::retryFeedsLoad,
            onFeedSearchQueryChanged = viewModel::onFeedSearchQueryChanged,
            onFollowFeedClick = viewModel::onFollowFeedClick,
            onUnfollowFeedClick = viewModel::onUnfollowFeedClick,
            onThemeSelected = viewModel::onThemeSelected,
            onLanguageSelected = viewModel::onLanguageSelected,
            onNotificationsEnabledChanged = viewModel::onNotificationsEnabledChanged,
            onSuggestionsOutsideChanged = viewModel::onSuggestionsOutsideChanged,
            onFeedScheduleChanged = viewModel::onFeedScheduleChanged,
        )

        if (showSnackBar) {
            key(snackBarId) {
                ThulurSnackBar(
                    message = snackBarMessage,
                    state = ThulurSnackBarState.Error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.thulurDp(), vertical = 24.thulurDp()),
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onRetryLoad: () -> Unit,
    onSectionSelected: (SettingsSection) -> Unit,
    onTerminateSessionClick: (String) -> Unit,
    onRetryFeedsLoad: () -> Unit,
    onFeedSearchQueryChanged: (String) -> Unit,
    onFollowFeedClick: (String) -> Unit,
    onUnfollowFeedClick: (String) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    onSuggestionsOutsideChanged: (Boolean) -> Unit,
    onFeedScheduleChanged: (FeedScheduleValue) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val leftRailWidth = 225.thulurDp()
    val selectorStartPadding = 30.thulurDp()
    val selectorTopPadding = 70.thulurDp()
    val contentBottomPadding = 15.thulurDp()
    val contentPadding = 30.thulurDp()
    val sectionSpacing = 56.thulurDp()
    val subsectionSpacing = 24.thulurDp()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBackground)
            .safeContentPadding()
    ) {
        ThulurAppBar(
            title = "Settings",
            backLabel = "Daily Feed",
            onBackClick = onBackClick,
        )

        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .width(leftRailWidth)
                    .fillMaxHeight()
                    .background(colors.railColor),
            ) {
                SettingsSectionSelector(
                    selectedSection = uiState.selectedSection,
                    onSectionSelected = onSectionSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = selectorStartPadding,
                            top = selectorTopPadding,
                        ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = contentPadding,
                        top = contentPadding,
                        end = contentPadding,
                        bottom = contentBottomPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing),
            ) {
                when (uiState.selectedSection) {
                    SettingsSection.AccountAndApp,
                    SettingsSection.Subscription,
                    -> {
                        SettingsAccountSection(
                            state = uiState.accountState,
                            onTerminateSessionClick = onTerminateSessionClick,
                            sectionSpacing = subsectionSpacing,
                        )

                        SettingsAppSection(
                            contentState = uiState.contentState,
                            state = uiState.appState,
                            onRetryLoad = onRetryLoad,
                            onThemeSelected = onThemeSelected,
                            onLanguageSelected = onLanguageSelected,
                            onNotificationsEnabledChanged = onNotificationsEnabledChanged,
                            onSuggestionsOutsideChanged = onSuggestionsOutsideChanged,
                            onFeedScheduleChanged = onFeedScheduleChanged,
                        )
                    }

                    SettingsSection.Feeds -> {
                        SettingsFeedsSection(
                            state = uiState.feedsState,
                            onRetryFeedsLoad = onRetryFeedsLoad,
                            onFeedSearchQueryChanged = onFeedSearchQueryChanged,
                            onFollowFeedClick = onFollowFeedClick,
                            onUnfollowFeedClick = onUnfollowFeedClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsAccountSection(
    state: SettingsAccountState,
    onTerminateSessionClick: (String) -> Unit,
    sectionSpacing: androidx.compose.ui.unit.Dp,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Column(
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        BasicText(
            text = "Account",
            style = typography.settingsSectionTitle.copy(
                color = colors.sectionTitleColor,
            ),
        )

        if (state.isLoading) {
            BasicText(
                text = "Loading account information...",
                style = typography.settingsBody.copy(
                    color = colors.bodyMutedColor,
                ),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
        ) {
            BasicText(
                text = "Email",
                style = typography.settingsSubsectionTitle.copy(
                    color = colors.subsectionTitleColor,
                ),
            )
            BasicText(
                text = state.currentEmail.orEmpty(),
                style = typography.settingsBody.copy(
                    color = colors.bodyColor,
                ),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.thulurDp()),
        ) {
            BasicText(
                text = "Sessions",
                style = typography.settingsSubsectionTitle.copy(
                    color = colors.subsectionTitleColor,
                ),
            )

            if (state.sessions.isEmpty() && !state.isLoading) {
                BasicText(
                    text = "No active sessions.",
                    style = typography.settingsBody.copy(
                        color = colors.bodyMutedColor,
                    ),
                )
            } else {
                state.sessions.forEach { session ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
                    ) {
                        SessionItem(
                            session = session,
                            onTerminateClick = { onTerminateSessionClick(session.id) },
                            isTerminating = session.id in state.terminatingSessionIds,
                        )
                        SettingsDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsAppSection(
    contentState: SettingsContentState,
    state: SettingsAppState,
    onRetryLoad: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    onSuggestionsOutsideChanged: (Boolean) -> Unit,
    onFeedScheduleChanged: (FeedScheduleValue) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Column(
        verticalArrangement = Arrangement.spacedBy(24.thulurDp()),
    ) {
        BasicText(
            text = "App",
            style = typography.settingsSectionTitle.copy(
                color = colors.sectionTitleColor,
            ),
        )

        when (contentState) {
            SettingsContentState.Loading -> BasicText(
                text = "Loading app settings...",
                style = typography.settingsBody.copy(
                    color = colors.bodyMutedColor,
                ),
            )

            is SettingsContentState.Error -> {
                BasicText(
                    text = contentState.message,
                    style = typography.settingsBody.copy(
                        color = colors.errorColor,
                    ),
                )
                SettingsInlineActionButton(
                    label = "Retry",
                    onClick = onRetryLoad,
                )
            }

            SettingsContentState.Ready -> {
                AppSettingItem(
                    title = "Feed Schedule",
                    supportingText = "Select time when feed should be ready. If today’s feed was made, changes will apply on the next day",
                ) {
                    SettingsTimeAction(
                        value = state.values.feedSchedule,
                        enabled = SettingsAppField.FeedSchedule !in state.pendingFields,
                        onTimeSelected = onFeedScheduleChanged,
                    )
                }

                AppSettingItem(
                    title = "Theme",
                ) {
                    SettingsDropdownAction(
                        selectedValue = state.values.theme.displayLabel(),
                        options = ThemeMode.entries.map { it.displayLabel() },
                        enabled = SettingsAppField.Theme !in state.pendingFields,
                        onOptionSelected = { label ->
                            onThemeSelected(
                                if (label == ThemeMode.Dark.displayLabel()) {
                                    ThemeMode.Dark
                                } else {
                                    ThemeMode.Light
                                },
                            )
                        },
                    )
                }

                AppSettingItem(
                    title = "Language",
                    supportingText = "Effects the next generated feed",
                ) {
                    SettingsDropdownAction(
                        selectedValue = state.values.language,
                        options = optionsWithCurrent(
                            current = state.values.language,
                            baseOptions = SettingsLanguageOptions,
                        ),
                        enabled = SettingsAppField.Language !in state.pendingFields,
                        onOptionSelected = onLanguageSelected,
                    )
                }

                AppSettingItem(
                    title = "Notification",
                    supportingText = "Notify, when the feed is ready",
                ) {
                    SettingsSwitchAction(
                        checked = state.values.notificationsEnabled,
                        enabled = SettingsAppField.Notification !in state.pendingFields,
                        onCheckedChange = onNotificationsEnabledChanged,
                    )
                }

                AppSettingItem(
                    title = "Suggestions Outside",
                    supportingText = "Show suggestions outside the scheduled feed cycle",
                ) {
                    SettingsSwitchAction(
                        checked = state.values.suggestionsOutside,
                        enabled = SettingsAppField.SuggestionsOutside !in state.pendingFields,
                        onCheckedChange = onSuggestionsOutsideChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsInlineActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    ThulurButton(
        text = label,
        onClick = onClick,
        enabled = enabled,
        colorRole = ThulurColorRole.Primary,
        useContainerStates = false,
        stateColorsOverride = colors.inlineActionButton,
        textStyle = typography.settingsAction,
        contentPadding = PaddingValues(),
        trailingIcon = trailingIcon,
    )
}

@Composable
private fun SettingsDropdownAction(
    selectedValue: String,
    options: List<String>,
    enabled: Boolean,
    onOptionSelected: (String) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    var expanded by remember { mutableStateOf(false) }

    Box {
        SettingsInlineActionButton(
            label = selectedValue,
            onClick = { expanded = true },
            enabled = enabled,
        )

        SettingsDropdown(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.dropdownMenuContainerColor,
        ) {
            Column {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .clickable {
                                expanded = false
                                onOptionSelected(option)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        BasicText(
                            text = option,
                            style = ThulurTheme.SemanticTypography.settingsBody.copy(
                                color = colors.dropdownMenuContentColor,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTimeAction(
    value: FeedScheduleValue,
    enabled: Boolean,
    onTimeSelected: (FeedScheduleValue) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    var expanded by remember { mutableStateOf(false) }
    var draftValue by remember(value) { mutableStateOf(value) }

    LaunchedEffect(value, expanded) {
        if (!expanded) {
            draftValue = value
        }
    }

    Box {
        SettingsInlineActionButton(
            label = value.toDisplayLabel(),
            onClick = {
                draftValue = value
                expanded = true
            },
            enabled = enabled,
        )

        SettingsDropdown(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                if (draftValue != value) {
                    onTimeSelected(draftValue)
                }
            },
            containerColor = colors.dropdownMenuContainerColor,
            modifier = Modifier.padding(12.thulurDp()),
        ) {
            ThulurTimeSelector(
                hour = draftValue.hour,
                minute = draftValue.minute,
                enabled = enabled,
                onTimeChange = { hour, minute ->
                    draftValue = FeedScheduleValue(hour = hour, minute = minute)
                },
            )
        }
    }
}

@Composable
private fun SettingsSwitchAction(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.switchCheckedThumbColor,
            checkedTrackColor = colors.switchCheckedTrackColor,
            uncheckedThumbColor = colors.switchUncheckedThumbColor,
            uncheckedTrackColor = colors.switchUncheckedTrackColor,
            disabledCheckedThumbColor = colors.switchDisabledThumbColor,
            disabledCheckedTrackColor = colors.switchDisabledTrackColor,
            disabledUncheckedThumbColor = colors.switchDisabledThumbColor,
            disabledUncheckedTrackColor = colors.switchDisabledTrackColor,
        ),
    )
}

@Composable
private fun SettingsDivider() {
    val colors = ThulurTheme.SemanticColors.settingsScreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.dividerColor),
    )
}

private fun optionsWithCurrent(
    current: String,
    baseOptions: List<String>,
): List<String> = buildList {
    if (current.isNotBlank() && current !in baseOptions) {
        add(current)
    }
    addAll(baseOptions)
}

private fun ThemeMode.displayLabel(): String = when (this) {
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

private fun FeedScheduleValue.toDisplayLabel(): String =
    "${hour}:${minute.toString().padStart(2, '0')}"

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            SettingsScreen(
                uiState = SettingsUiState(
                    contentState = SettingsContentState.Ready,
                ),
                onBackClick = {},
                onRetryLoad = {},
                onSectionSelected = {},
                onTerminateSessionClick = {},
                onRetryFeedsLoad = {},
                onFeedSearchQueryChanged = {},
                onFollowFeedClick = {},
                onUnfollowFeedClick = {},
                onThemeSelected = {},
                onLanguageSelected = {},
                onNotificationsEnabledChanged = {},
                onSuggestionsOutsideChanged = {},
                onFeedScheduleChanged = {},
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Dark) {
            SettingsScreen(
                uiState = SettingsUiState(
                    contentState = SettingsContentState.Ready,
                    appState = SettingsAppState(
                        values = SettingsAppValues(
                            theme = ThemeMode.Dark,
                            notificationsEnabled = false,
                            suggestionsOutside = false,
                        ),
                    ),
                ),
                onBackClick = {},
                onRetryLoad = {},
                onSectionSelected = {},
                onTerminateSessionClick = {},
                onRetryFeedsLoad = {},
                onFeedSearchQueryChanged = {},
                onFollowFeedClick = {},
                onUnfollowFeedClick = {},
                onThemeSelected = {},
                onLanguageSelected = {},
                onNotificationsEnabledChanged = {},
                onSuggestionsOutsideChanged = {},
                onFeedScheduleChanged = {},
            )
        }
    }
}
