package com.example.thulur.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thulur.domain.model.Feed
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurTextField
import com.example.thulur.presentation.settings.SettingsFeedsState
import com.example.thulur.presentation.settings.toSettingsFeedChips
import com.example.thulur.presentation.settings.toSettingsFeedTitle
import com.example.thulur.presentation.theme.ThulurButtonStateSemanticColors
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun SettingsFeedsSection(
    state: SettingsFeedsState,
    onRetryFeedsLoad: () -> Unit,
    onFeedSearchQueryChanged: (String) -> Unit,
    onFollowFeedClick: (String) -> Unit,
    onUnfollowFeedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography
    val hasAnyData = state.followedFeeds.isNotEmpty() || state.catalogFeeds.isNotEmpty()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.thulurDp()),
    ) {
        when {
            state.isLoading && !hasAnyData -> {
                BasicText(
                    text = "Loading feeds...",
                    style = typography.settingsBody.copy(
                        color = colors.bodyMutedColor,
                    ),
                )
            }

            state.errorMessage != null && !hasAnyData -> {
                BasicText(
                    text = state.errorMessage,
                    style = typography.settingsBody.copy(
                        color = colors.errorColor,
                    ),
                )
                ThulurButton(
                    text = "Retry",
                    onClick = onRetryFeedsLoad,
                    colorRole = ThulurColorRole.Primary,
                    useContainerStates = false,
                    stateColorsOverride = colors.inlineActionButton,
                    textStyle = typography.settingsAction,
                    contentPadding = PaddingValues(),
                )
            }

            else -> {
                state.errorMessage?.let { message ->
                    BasicText(
                        text = message,
                        style = typography.settingsBody.copy(
                            color = colors.errorColor,
                        ),
                    )
                }

                UserFeedsSection(
                    feeds = state.followedFeeds,
                    pendingFeedIds = state.pendingUnfollowIds,
                    onUnfollowFeedClick = onUnfollowFeedClick,
                )

                AddNewFeedSection(
                    searchQuery = state.searchQuery,
                    availableFeeds = state.visibleAvailableFeeds,
                    pendingFollowIds = state.pendingFollowIds,
                    onFeedSearchQueryChanged = onFeedSearchQueryChanged,
                    onFollowFeedClick = onFollowFeedClick,
                )
            }
        }
    }
}

@Composable
private fun UserFeedsSection(
    feeds: List<Feed>,
    pendingFeedIds: Set<String>,
    onUnfollowFeedClick: (String) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Column(
        verticalArrangement = Arrangement.spacedBy(24.thulurDp()),
    ) {
        BasicText(
            text = "Your feeds",
            style = typography.settingsSectionTitle.copy(
                color = colors.sectionTitleColor,
            ),
        )

        if (feeds.isEmpty()) {
            BasicText(
                text = "No followed feeds yet.",
                style = typography.settingsBody.copy(
                    color = colors.bodyMutedColor,
                ),
            )
        } else {
            FeedList(
                feeds = feeds,
                pendingFeedIds = pendingFeedIds,
                actionLabel = "Unfollow",
                pendingActionLabel = "Unfollowing...",
                onActionClick = onUnfollowFeedClick,
                actionStateColors = colors.terminateButton,
                actionColorRole = ThulurColorRole.Error,
                showUrl = false,
                showChips = false,
            )
        }
    }
}

@Composable
private fun AddNewFeedSection(
    searchQuery: String,
    availableFeeds: List<Feed>,
    pendingFollowIds: Set<String>,
    onFeedSearchQueryChanged: (String) -> Unit,
    onFollowFeedClick: (String) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Column(
        verticalArrangement = Arrangement.spacedBy(20.thulurDp()),
    ) {
        BasicText(
            text = "Add new feed",
            style = typography.settingsSectionTitle.copy(
                color = colors.sectionTitleColor,
            ),
        )

        ThulurTextField(
            value = searchQuery,
            onValueChange = onFeedSearchQueryChanged,
            placeholder = "RSS Link or source/topic",
            modifier = Modifier
                .fillMaxWidth()
                .height(42.thulurDp()),
            stateColorsOverride = colors.inputField,
        )

        if (searchQuery.isNotBlank() && availableFeeds.isEmpty()) {
            FeedInfoCard(
                text = "No feeds found for this query.",
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.thulurDp()),
        ) {
            BasicText(
                text = "Available feeds",
                style = typography.settingsSubsectionTitle.copy(
                    color = colors.subsectionTitleColor,
                ),
            )

            when {
                availableFeeds.isEmpty() && searchQuery.isBlank() -> BasicText(
                    text = "No available feeds.",
                    style = typography.settingsBody.copy(
                        color = colors.bodyMutedColor,
                    ),
                )

                availableFeeds.isNotEmpty() -> FeedList(
                    feeds = availableFeeds,
                    pendingFeedIds = pendingFollowIds,
                    actionLabel = "Follow",
                    pendingActionLabel = "Following...",
                    onActionClick = onFollowFeedClick,
                    actionStateColors = colors.inlineActionButton,
                    actionColorRole = ThulurColorRole.Primary,
                    showUrl = true,
                    showChips = true,
                )
            }
        }
    }
}

@Composable
private fun FeedList(
    feeds: List<Feed>,
    pendingFeedIds: Set<String>,
    actionLabel: String,
    pendingActionLabel: String,
    onActionClick: (String) -> Unit,
    actionStateColors: ThulurButtonStateSemanticColors,
    actionColorRole: ThulurColorRole,
    showUrl: Boolean,
    showChips: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
    ) {
        feeds.forEach { feed ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
            ) {
                FeedItem(
                    feed = feed,
                    actionLabel = actionLabel,
                    pendingActionLabel = pendingActionLabel,
                    isPending = feed.id in pendingFeedIds,
                    onActionClick = { onActionClick(feed.id) },
                    actionStateColors = actionStateColors,
                    actionColorRole = actionColorRole,
                    showUrl = showUrl,
                    showChips = showChips,
                )
                FeedSectionDivider()
            }
        }
    }
}

@Composable
private fun FeedItem(
    feed: Feed,
    actionLabel: String,
    pendingActionLabel: String,
    isPending: Boolean,
    onActionClick: () -> Unit,
    actionStateColors: ThulurButtonStateSemanticColors,
    actionColorRole: ThulurColorRole,
    showUrl: Boolean,
    showChips: Boolean,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography
    val chips = feed.toSettingsFeedChips()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.thulurDp()),
        horizontalArrangement = Arrangement.spacedBy(20.thulurDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.thulurDp()),
        ) {
            BasicText(
                text = feed.toSettingsFeedTitle(),
                style = typography.settingsSubsectionTitle.copy(
                    color = colors.bodyColor,
                ),
            )

            if (showUrl) {
                BasicText(
                    text = feed.url,
                    style = typography.settingsBody.copy(
                        color = colors.bodyMutedColor,
                    ),
                )
            }

            if (showChips && chips.isNotEmpty()) {
                FeedChipsRow(
                    chips = chips,
                )
            }
        }

        ThulurButton(
            text = if (isPending) pendingActionLabel else actionLabel,
            onClick = onActionClick,
            enabled = !isPending,
            colorRole = actionColorRole,
            useContainerStates = false,
            stateColorsOverride = actionStateColors,
            textStyle = typography.settingsAction,
            contentPadding = PaddingValues(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedChipsRow(
    chips: List<String>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.thulurDp()),
        verticalArrangement = Arrangement.spacedBy(8.thulurDp()),
    ) {
        chips.forEach { chip ->
            FeedTagItem(label = chip)
        }
    }
}

@Composable
private fun FeedTagItem(
    label: String,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.thulurDp()))
            .background(colors.railColor)
            .padding(
                horizontal = 10.thulurDp(),
                vertical = 4.thulurDp(),
            ),
    ) {
        BasicText(
            text = label,
            style = typography.settingsMeta.copy(
                color = colors.bodyMutedColor,
            ),
        )
    }
}

@Composable
private fun FeedInfoCard(
    text: String,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.thulurDp()))
            .background(colors.railColor)
            .padding(16.thulurDp()),
    ) {
        BasicText(
            text = text,
            style = typography.settingsBody.copy(
                color = colors.bodyMutedColor,
            ),
        )
    }
}

@Composable
private fun FeedSectionDivider() {
    val colors = ThulurTheme.SemanticColors.settingsScreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.dividerColor),
    )
}

@Preview
@Composable
private fun SettingsFeedsSectionPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            SettingsFeedsSection(
                state = SettingsFeedsState(
                    followedFeeds = listOf(
                        sampleFeed(
                            id = "feed-followed-1",
                            url = "https://www.wyborcza.pl/tech/rss.xml",
                            language = "pl",
                            tags = listOf("tech"),
                        ),
                    ),
                    catalogFeeds = listOf(
                        sampleFeed(
                            id = "feed-followed-1",
                            url = "https://www.wyborcza.pl/tech/rss.xml",
                            language = "pl",
                            tags = listOf("tech"),
                        ),
                        sampleFeed(
                            id = "feed-available-1",
                            url = "https://techcrunch.com/tag/ai/feed/",
                            language = "en",
                            tags = listOf("ai", "programming"),
                        ),
                    ),
                    visibleAvailableFeeds = listOf(
                        sampleFeed(
                            id = "feed-available-1",
                            url = "https://techcrunch.com/tag/ai/feed/",
                            language = "en",
                            tags = listOf("ai", "programming"),
                        ),
                    ),
                    isLoading = false,
                ),
                onRetryFeedsLoad = {},
                onFeedSearchQueryChanged = {},
                onFollowFeedClick = {},
                onUnfollowFeedClick = {},
            )
        }
    }
}

private fun sampleFeed(
    id: String,
    url: String,
    language: String,
    tags: List<String>,
): Feed = Feed(
    id = id,
    url = url,
    language = language,
    tags = tags,
    createdAt = "2026-04-19T08:00:00Z",
)
