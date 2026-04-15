package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

internal enum class DesktopScaleBucket(
    val factor: Float,
) {
    Compact(0.75f),
    Medium(0.875f),
    Base(1f),
    Wide(1.125f),
}

@Composable
actual fun rememberPlatformDesignScale(): ThulurDesignScale {
    val density = LocalDensity.current
    val widthPx = LocalWindowInfo.current.containerSize.width
    val widthDp = with(density) { widthPx.toDp().value }

    val bucket = remember(widthDp) {
        when {
            widthDp <= 0f -> DesktopScaleBucket.Base
            widthDp <= 1365f -> DesktopScaleBucket.Compact
            widthDp <= 1599f -> DesktopScaleBucket.Medium
            widthDp <= 2239f -> DesktopScaleBucket.Base
            else -> DesktopScaleBucket.Wide
        }
    }

    return remember(bucket) {
        ThulurDesignScale(factor = bucket.factor)
    }
}
