package com.example.thulur.presentation.settings

import com.example.thulur.domain.model.Feed
import com.example.thulur.presentation.composables.extractSourceLabel

internal fun Feed.toSettingsFeedTitle(): String = extractSourceLabel(url) ?: url

internal fun Feed.toSettingsFeedChips(): List<String> = buildList {
    language?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let(::add)

    tags.asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .forEach(::add)
}

internal fun SettingsFeedsState.recomputeVisibleAvailableFeeds(): SettingsFeedsState = copy(
    visibleAvailableFeeds = computeVisibleAvailableFeeds(
        catalogFeeds = catalogFeeds,
        followedFeeds = followedFeeds,
        searchQuery = searchQuery,
    ),
)

internal fun computeVisibleAvailableFeeds(
    catalogFeeds: List<Feed>,
    followedFeeds: List<Feed>,
    searchQuery: String,
): List<Feed> {
    val followedIds = followedFeeds.map(Feed::id).toSet()
    val normalizedQuery = searchQuery.trim()

    return catalogFeeds
        .asSequence()
        .filterNot { feed -> feed.id in followedIds }
        .filter { feed ->
            normalizedQuery.isBlank() ||
                feed.url.contains(normalizedQuery, ignoreCase = true) ||
                feed.toSettingsFeedChips().any { chip ->
                    chip.contains(normalizedQuery, ignoreCase = true)
                }
        }
        .toList()
}
