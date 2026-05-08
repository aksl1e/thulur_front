package com.example.thulur.presentation.root

enum class AppSubscriptionTier {
    Unknown,
    Free,
    Pro,
    Corporate,
}

internal fun String.toAppSubscriptionTier(): AppSubscriptionTier = when (lowercase()) {
    "free" -> AppSubscriptionTier.Free
    "pro" -> AppSubscriptionTier.Pro
    "corporate" -> AppSubscriptionTier.Corporate
    else -> AppSubscriptionTier.Unknown
}

internal fun AppSubscriptionTier.canDiscussThread(): Boolean = when (this) {
    AppSubscriptionTier.Pro,
    AppSubscriptionTier.Corporate -> true

    AppSubscriptionTier.Unknown,
    AppSubscriptionTier.Free -> false
}
