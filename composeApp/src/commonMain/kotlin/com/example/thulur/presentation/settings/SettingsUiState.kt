package com.example.thulur.presentation.settings

import com.example.thulur.domain.model.Feed
import com.example.thulur.presentation.theme.ThemeMode

data class SettingsUiState(
    val selectedSection: SettingsSection = SettingsSection.AccountAndApp,
    val contentState: SettingsContentState = SettingsContentState.Loading,
    val accountState: SettingsAccountState = SettingsAccountState(),
    val appState: SettingsAppState = SettingsAppState(),
    val feedsState: SettingsFeedsState = SettingsFeedsState(),
)

enum class SettingsSection {
    AccountAndApp,
    Subscription,
    Feeds,
}

sealed interface SettingsContentState {
    data object Loading : SettingsContentState

    data object Ready : SettingsContentState

    data class Error(
        val message: String,
    ) : SettingsContentState
}

data class SettingsAccountState(
    val currentEmail: String? = null,
    val sessions: List<SettingsSessionState> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val terminatingSessionIds: Set<String> = emptySet(),
)

data class SettingsSessionState(
    val id: String,
    val title: String,
    val clientLabel: String,
    val metaLabel: String,
)

data class SettingsAppState(
    val values: SettingsAppValues = SettingsAppValues(),
    val pendingFields: Set<SettingsAppField> = emptySet(),
    val errorMessage: String? = null,
)

data class SettingsFeedsState(
    val followedFeeds: List<Feed> = emptyList(),
    val catalogFeeds: List<Feed> = emptyList(),
    val searchQuery: String = "",
    val visibleAvailableFeeds: List<Feed> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val pendingFollowIds: Set<String> = emptySet(),
    val pendingUnfollowIds: Set<String> = emptySet(),
)

data class SettingsAppValues(
    val feedSchedule: FeedScheduleValue = FeedScheduleValue(hour = 8, minute = 0),
    val theme: ThemeMode = ThemeMode.Light,
    val language: String = DEFAULT_LANGUAGE,
    val notificationsEnabled: Boolean = true,
    val timezone: String = DEFAULT_TIMEZONE,
    val suggestionsOutside: Boolean = true,
)

data class FeedScheduleValue(
    val hour: Int,
    val minute: Int,
)

enum class SettingsAppField {
    FeedSchedule,
    Theme,
    Language,
    Notification,
    Timezone,
    SuggestionsOutside,
}

internal const val DEFAULT_LANGUAGE = "English"
internal const val DEFAULT_TIMEZONE = "Europe/Warsaw"

internal val SettingsLanguageOptions = listOf(
    "English",
    "Polish",
)

internal val SettingsTimezoneOptions = listOf(
    "UTC",
    "Europe/Warsaw",
    "Europe/Berlin",
    "Europe/London",
    "America/New_York",
    "America/Los_Angeles",
)
