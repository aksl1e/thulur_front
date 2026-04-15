package com.example.thulur.theme

package com.example.thulur.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class ThulurDesignScale(
    val factor: Float = 1f,
)

val LocalThulurDesignScale = staticCompositionLocalOf { ThulurDesignScale() }

@Composable
expect fun rememberPlatformDesignScale(): ThulurDesignScale

@Composable
fun ProvideThulurDesignScale(
    content: @Composable () -> Unit,
) {
    val scale = rememberPlatformDesignScale()
    ProvideThulurDesignScale(scale = scale, content = content)
}

@Composable
fun ProvideThulurDesignScale(
    scale: ThulurDesignScale,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalThulurDesignScale provides scale,
        content = content,
    )
}

@Composable
@ReadOnlyComposable
fun Int.thulurDp(): Dp = (this * LocalThulurDesignScale.current.factor).dp

@Composable
@ReadOnlyComposable
fun Int.thulurSp(): TextUnit = (this * LocalThulurDesignScale.current.factor).sp
