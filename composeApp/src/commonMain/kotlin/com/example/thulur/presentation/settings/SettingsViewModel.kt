package com.example.thulur.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.usecase.FollowFeedUseCase
import com.example.thulur.domain.usecase.GetAllFeedsUseCase
import com.example.thulur.domain.usecase.GetAuthSessionsUseCase
import com.example.thulur.domain.usecase.GetCurrentUserUseCase
import com.example.thulur.domain.usecase.GetFollowedFeedsUseCase
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.domain.usecase.PatchUserSettingsUseCase
import com.example.thulur.domain.usecase.TerminateAuthSessionUseCase
import com.example.thulur.domain.usecase.UnfollowFeedUseCase
import com.example.thulur.presentation.theme.ThemeMode
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

sealed class SettingsSnackBarEvent {
    data class Error(val message: String) : SettingsSnackBarEvent()
}

class SettingsViewModel(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val patchUserSettingsUseCase: PatchUserSettingsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAuthSessionsUseCase: GetAuthSessionsUseCase,
    private val terminateAuthSessionUseCase: TerminateAuthSessionUseCase,
    private val getFollowedFeedsUseCase: GetFollowedFeedsUseCase,
    private val getAllFeedsUseCase: GetAllFeedsUseCase,
    private val followFeedUseCase: FollowFeedUseCase,
    private val unfollowFeedUseCase: UnfollowFeedUseCase,
) : ViewModel() {
    private var appLoadJob: Job? = null
    private var accountLoadJob: Job? = null
    private var feedsLoadJob: Job? = null
    private var confirmedAppValues = SettingsAppValues()
    private var hasLoadedAppValues = false

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _snackBarEvents = Channel<SettingsSnackBarEvent>(Channel.BUFFERED)
    val snackBarEvents = _snackBarEvents.receiveAsFlow()

    init {
        loadAll()
    }

    fun retryLoad() {
        loadAppSettings()
        loadAccountData()
    }

    fun retryFeedsLoad() {
        loadFeeds()
    }

    fun onSectionSelected(section: SettingsSection) {
        if (section == SettingsSection.Subscription) return

        _uiState.update { state ->
            state.copy(selectedSection = section)
        }
    }

    fun onFeedSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                feedsState = state.feedsState.copy(
                    searchQuery = query,
                ).recomputeVisibleAvailableFeeds(),
            )
        }
    }

    fun onFollowFeedClick(identifier: String) {
        val feedsState = _uiState.value.feedsState
        if (identifier in feedsState.pendingFollowIds || identifier in feedsState.pendingUnfollowIds) return

        _uiState.update { state ->
            state.copy(
                feedsState = state.feedsState.copy(
                    pendingFollowIds = state.feedsState.pendingFollowIds + identifier,
                ),
            )
        }

        viewModelScope.launch {
            try {
                followFeedUseCase(identifier = identifier)
                _uiState.update { state ->
                    state.copy(
                        feedsState = state.feedsState.copy(
                            pendingFollowIds = state.feedsState.pendingFollowIds - identifier,
                            searchQuery = "",
                        ).recomputeVisibleAvailableFeeds(),
                    )
                }
                loadFeeds()
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        feedsState = state.feedsState.copy(
                            pendingFollowIds = state.feedsState.pendingFollowIds - identifier,
                        ),
                    )
                }
                _snackBarEvents.send(SettingsSnackBarEvent.Error(throwable.message ?: "Failed to follow feed."))
            }
        }
    }

    fun onUnfollowFeedClick(feedId: String) {
        val feedsState = _uiState.value.feedsState
        if (feedId in feedsState.pendingUnfollowIds || feedId in feedsState.pendingFollowIds) return

        if (feedsState.followedFeeds.none { it.id == feedId }) return

        _uiState.update { state ->
            state.copy(
                feedsState = state.feedsState.copy(
                    pendingUnfollowIds = state.feedsState.pendingUnfollowIds + feedId,
                ),
            )
        }

        viewModelScope.launch {
            try {
                unfollowFeedUseCase(feedId = feedId)
                _uiState.update { state ->
                    state.copy(
                        feedsState = state.feedsState.copy(
                            followedFeeds = state.feedsState.followedFeeds.filterNot { it.id == feedId },
                            pendingUnfollowIds = state.feedsState.pendingUnfollowIds - feedId,
                        ).recomputeVisibleAvailableFeeds(),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        feedsState = state.feedsState.copy(
                            pendingUnfollowIds = state.feedsState.pendingUnfollowIds - feedId,
                        ),
                    )
                }
                _snackBarEvents.send(SettingsSnackBarEvent.Error(throwable.message ?: "Failed to unfollow feed."))
            }
        }
    }

    fun onTerminateSessionClick(sessionId: String) {
        if (sessionId in _uiState.value.accountState.terminatingSessionIds) return

        _uiState.update { state ->
            state.copy(
                accountState = state.accountState.copy(
                    terminatingSessionIds = state.accountState.terminatingSessionIds + sessionId,
                ),
            )
        }

        viewModelScope.launch {
            try {
                terminateAuthSessionUseCase(sessionId = sessionId)
                val refreshedSessions = getAuthSessionsUseCase().map(AuthSession::toSettingsSessionState)
                _uiState.update { state ->
                    state.copy(
                        accountState = state.accountState.copy(
                            sessions = refreshedSessions,
                            terminatingSessionIds = state.accountState.terminatingSessionIds - sessionId,
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        accountState = state.accountState.copy(
                            terminatingSessionIds = state.accountState.terminatingSessionIds - sessionId,
                        ),
                    )
                }
                _snackBarEvents.send(SettingsSnackBarEvent.Error(throwable.message ?: "Failed to terminate session."))
            }
        }
    }

    fun onThemeSelected(theme: ThemeMode) {
        if (theme == confirmedAppValues.theme) return
        mutateAppSettings(
            field = SettingsAppField.Theme,
            optimisticValues = _uiState.value.appState.values.copy(theme = theme),
            update = PatchUserSettings(
                darkMode = theme == ThemeMode.Dark,
            ),
        )
    }

    fun onLanguageSelected(language: String) {
        if (language == confirmedAppValues.language) return
        mutateAppSettings(
            field = SettingsAppField.Language,
            optimisticValues = _uiState.value.appState.values.copy(language = language),
            update = PatchUserSettings(
                language = language.toApiLanguageCode(),
            ),
        )
    }

    fun onTimezoneSelected(timezone: String) {
        if (timezone == confirmedAppValues.timezone) return
        mutateAppSettings(
            field = SettingsAppField.Timezone,
            optimisticValues = _uiState.value.appState.values.copy(timezone = timezone),
            update = PatchUserSettings(
                timezone = timezone,
            ),
        )
    }

    fun onNotificationsEnabledChanged(enabled: Boolean) {
        if (enabled == confirmedAppValues.notificationsEnabled) return
        mutateAppSettings(
            field = SettingsAppField.Notification,
            optimisticValues = _uiState.value.appState.values.copy(notificationsEnabled = enabled),
            update = PatchUserSettings(
                notificationsEnabled = enabled,
            ),
        )
    }

    fun onSuggestionsOutsideChanged(enabled: Boolean) {
        if (enabled == confirmedAppValues.suggestionsOutside) return
        mutateAppSettings(
            field = SettingsAppField.SuggestionsOutside,
            optimisticValues = _uiState.value.appState.values.copy(suggestionsOutside = enabled),
            update = PatchUserSettings(
                suggestionsOutside = enabled,
            ),
        )
    }

    fun onFeedScheduleChanged(value: FeedScheduleValue) {
        if (value.normalized() == confirmedAppValues.feedSchedule) return
        mutateAppSettings(
            field = SettingsAppField.FeedSchedule,
            optimisticValues = _uiState.value.appState.values.copy(feedSchedule = value.normalized()),
            update = PatchUserSettings(
                notificationsTime = value.normalized().toBackendTime(),
            ),
        )
    }

    private fun loadAll() {
        loadAppSettings()
        loadAccountData()
        loadFeeds()
    }

    private fun loadAppSettings() {
        appLoadJob?.cancel()
        appLoadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    contentState = SettingsContentState.Loading,
                    appState = state.appState.copy(
                        pendingFields = emptySet(),
                    ),
                )
            }

            try {
                val settings = getUserSettingsUseCase()
                confirmedAppValues = settings.toAppValues()
                hasLoadedAppValues = true

                _uiState.update { state ->
                    state.copy(
                        contentState = SettingsContentState.Ready,
                        appState = SettingsAppState(
                            values = confirmedAppValues,
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        contentState = SettingsContentState.Error(
                            message = throwable.message ?: "Failed to load settings.",
                        ),
                        appState = state.appState.copy(
                            pendingFields = emptySet(),
                        ),
                    )
                }
            }
        }
    }

    private fun loadAccountData() {
        accountLoadJob?.cancel()
        accountLoadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    accountState = state.accountState.copy(
                        isLoading = true,
                    ),
                )
            }

            try {
                supervisorScope {
                    val currentUserDeferred = async { getCurrentUserResult() }
                    val sessionsDeferred = async { getAuthSessionsResult() }

                    val currentUserResult = currentUserDeferred.await()
                    val sessionsResult = sessionsDeferred.await()

                    val errorMsg = currentUserResult.exceptionOrNull()?.message
                        ?: sessionsResult.exceptionOrNull()?.message

                    _uiState.update { state ->
                        state.copy(
                            accountState = state.accountState.copy(
                                currentEmail = currentUserResult.getOrNull()?.email,
                                sessions = sessionsResult.getOrNull()?.map(AuthSession::toSettingsSessionState)
                                    ?: emptyList(),
                                isLoading = false,
                            ),
                        )
                    }

                    errorMsg?.let { _snackBarEvents.send(SettingsSnackBarEvent.Error(it)) }
                }
            } catch (exception: CancellationException) {
                throw exception
            }
        }
    }

    private fun loadFeeds() {
        feedsLoadJob?.cancel()
        feedsLoadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    feedsState = state.feedsState.copy(
                        isLoading = true,
                        isError = false,
                    ),
                )
            }

            try {
                supervisorScope {
                    val followedFeedsDeferred = async { getFollowedFeedsResult() }
                    val allFeedsDeferred = async { getAllFeedsResult() }

                    val followedFeedsResult = followedFeedsDeferred.await()
                    val allFeedsResult = allFeedsDeferred.await()

                    val errorMsg = followedFeedsResult.exceptionOrNull()?.message
                        ?: allFeedsResult.exceptionOrNull()?.message

                    _uiState.update { state ->
                        val updatedFollowedFeeds = followedFeedsResult.getOrNull() ?: state.feedsState.followedFeeds
                        val updatedCatalogFeeds = allFeedsResult.getOrNull() ?: state.feedsState.catalogFeeds

                        state.copy(
                            feedsState = state.feedsState.copy(
                                followedFeeds = updatedFollowedFeeds,
                                catalogFeeds = updatedCatalogFeeds,
                                isLoading = false,
                                isError = errorMsg != null,
                                pendingFollowIds = emptySet(),
                                pendingUnfollowIds = emptySet(),
                            ).recomputeVisibleAvailableFeeds(),
                        )
                    }

                    errorMsg?.let { _snackBarEvents.send(SettingsSnackBarEvent.Error(it)) }
                }
            } catch (exception: CancellationException) {
                throw exception
            }
        }
    }

    private fun mutateAppSettings(
        field: SettingsAppField,
        optimisticValues: SettingsAppValues,
        update: PatchUserSettings,
    ) {
        if (!hasLoadedAppValues) return
        if (field in _uiState.value.appState.pendingFields) return

        _uiState.update { state ->
            state.copy(
                contentState = SettingsContentState.Ready,
                appState = state.appState.copy(
                    values = optimisticValues,
                    pendingFields = state.appState.pendingFields + field,
                ),
            )
        }

        viewModelScope.launch {
            try {
                val updatedSettings = patchUserSettingsUseCase(update = update)
                confirmedAppValues = updatedSettings.toAppValues()

                _uiState.update { state ->
                    val pendingFields = state.appState.pendingFields - field
                    state.copy(
                        contentState = SettingsContentState.Ready,
                        appState = state.appState.copy(
                            values = mergePendingValues(
                                confirmedValues = confirmedAppValues,
                                currentValues = state.appState.values,
                                pendingFields = pendingFields,
                            ),
                            pendingFields = pendingFields,
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    val pendingFields = state.appState.pendingFields - field
                    state.copy(
                        contentState = SettingsContentState.Ready,
                        appState = state.appState.copy(
                            values = mergePendingValues(
                                confirmedValues = confirmedAppValues,
                                currentValues = state.appState.values,
                                pendingFields = pendingFields,
                            ),
                            pendingFields = pendingFields,
                        ),
                    )
                }
                _snackBarEvents.send(SettingsSnackBarEvent.Error(throwable.message ?: "Failed to update setting."))
            }
        }
    }

    private fun mergePendingValues(
        confirmedValues: SettingsAppValues,
        currentValues: SettingsAppValues,
        pendingFields: Set<SettingsAppField>,
    ): SettingsAppValues = pendingFields.fold(confirmedValues) { values, field ->
        values.copyFieldFrom(field = field, source = currentValues)
    }

    private suspend fun getCurrentUserResult(): Result<CurrentUser> = try {
        Result.success(getCurrentUserUseCase())
    } catch (exception: CancellationException) {
        throw exception
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

    private suspend fun getAuthSessionsResult(): Result<List<AuthSession>> = try {
        Result.success(getAuthSessionsUseCase())
    } catch (exception: CancellationException) {
        throw exception
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

    private suspend fun getFollowedFeedsResult(): Result<List<Feed>> = try {
        Result.success(getFollowedFeedsUseCase())
    } catch (exception: CancellationException) {
        throw exception
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

    private suspend fun getAllFeedsResult(): Result<List<Feed>> = try {
        Result.success(getAllFeedsUseCase())
    } catch (exception: CancellationException) {
        throw exception
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
}

internal fun settingsViewModelKey(sessionInstanceId: Int): String =
    "settings-session-$sessionInstanceId"

private fun SettingsAppValues.copyFieldFrom(
    field: SettingsAppField,
    source: SettingsAppValues,
): SettingsAppValues = when (field) {
    SettingsAppField.FeedSchedule -> copy(feedSchedule = source.feedSchedule)
    SettingsAppField.Theme -> copy(theme = source.theme)
    SettingsAppField.Language -> copy(language = source.language)
    SettingsAppField.Notification -> copy(notificationsEnabled = source.notificationsEnabled)
    SettingsAppField.Timezone -> copy(timezone = source.timezone)
    SettingsAppField.SuggestionsOutside -> copy(suggestionsOutside = source.suggestionsOutside)
}

private fun UserSettings.toAppValues(): SettingsAppValues = SettingsAppValues(
    feedSchedule = notificationsTime.toFeedScheduleValue(),
    theme = if (darkMode) ThemeMode.Dark else ThemeMode.Light,
    language = language.toDisplayLanguage(),
    notificationsEnabled = notificationsEnabled,
    timezone = timezone.ifBlank { DEFAULT_TIMEZONE },
    suggestionsOutside = suggestionsOutside,
)

private fun String.toFeedScheduleValue(): FeedScheduleValue {
    val match = Regex("""(\d{1,2}):(\d{1,2})""").find(this)
    val utcHour = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 8
    val utcMinute = match?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
    val systemTz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTz).date
    val utcDt = LocalDateTime(today, LocalTime(utcHour, utcMinute, 0, 0))
    val localDt = utcDt.toInstant(TimeZone.UTC).toLocalDateTime(systemTz)
    return FeedScheduleValue(hour = localDt.hour, minute = localDt.minute).normalized()
}

private fun FeedScheduleValue.normalized(): FeedScheduleValue = FeedScheduleValue(
    hour = hour.coerceIn(0, 24),
    minute = minute.coerceIn(0, 59),
)

private fun FeedScheduleValue.toBackendTime(): String {
    val systemTz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTz).date
    val localDt = LocalDateTime(today, LocalTime(hour, minute, 0, 0))
    val utcDt = localDt.toInstant(systemTz).toLocalDateTime(TimeZone.UTC)
    return "${utcDt.hour.toString().padStart(2, '0')}:${utcDt.minute.toString().padStart(2, '0')}:00.000Z"
}
private fun String.toDisplayLanguage(): String = when (lowercase()) {
    "polish", "pl", "pl-pl" -> "Polish"
    "english", "en", "en-us", "en-gb" -> "English"
    else -> ifBlank { DEFAULT_LANGUAGE }
}

private fun String.toApiLanguageCode(): String = when (this) {
    "Polish" -> "pl"
    "English" -> "en"
    else -> this
}


private fun AuthSession.toSettingsSessionState(): SettingsSessionState = SettingsSessionState(
    id = sessionId,
    title = deviceName,
    clientLabel = platform.toClientLabel(),
    metaLabel = buildMetaLabel(
        city = city,
        country = country,
        lastSeenAt = lastSeenAt,
    ),
)

private fun String.toClientLabel(): String = when (lowercase()) {
    "desktop" -> "Thulur Desktop"
    "mobile" -> "Thulur Mobile"
    else -> "Thulur ${toTitleWords()}"
}

private fun buildMetaLabel(
    city: String?,
    country: String?,
    lastSeenAt: String,
): String {
    val formattedDate = lastSeenAt.toMonthDayYear()
    return if (!city.isNullOrBlank() && !country.isNullOrBlank()) {
        "$city, $country - $formattedDate"
    } else {
        formattedDate
    }
}

private fun String.toMonthDayYear(): String {
    val match = Regex("""(\d{4})-(\d{2})-(\d{2})""").find(this) ?: return this
    val month = match.groupValues[2].toIntOrNull() ?: return this
    val day = match.groupValues[3].toIntOrNull() ?: return this
    val year = match.groupValues[1]
    return "$month/$day/$year"
}

private fun String.toTitleWords(): String = replace('-', ' ')
    .replace('_', ' ')
    .split(' ')
    .filter(String::isNotBlank)
    .joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { char ->
            if (char.isLowerCase()) {
                char.titlecase()
            } else {
                char.toString()
            }
        }
    }
    .ifBlank { "App" }
