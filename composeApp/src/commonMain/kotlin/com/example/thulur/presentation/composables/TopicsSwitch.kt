package com.example.thulur.presentation.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

enum class TopicsViewMode {
    TopicsAndArticles,
    TopicsOnly,
}

object TopicsSwitchDefaults {
    @Composable
    @ReadOnlyComposable
    fun textStyle() = ThulurTheme.SemanticTypography.topicsSwitchLabel
}

@Composable
fun TopicsSwitch(
    selected: TopicsViewMode,
    onSelect: (TopicsViewMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ThulurTheme.SemanticColors.topicsSwitch

    ThulurSegmentedSwitch(
        options = TopicsViewMode.entries,
        selected = selected,
        onSelect = onSelect,
        modifier = modifier,
        enabled = enabled,
        optionLabel = { option ->
            when (option) {
                TopicsViewMode.TopicsAndArticles -> "Topics & Articles"
                TopicsViewMode.TopicsOnly -> "Topics Only"
            }
        },
        textStyle = TopicsSwitchDefaults.textStyle(),
        colorsOverride = colors,
        horizontalItemPadding = 15.thulurDp(),
        verticalItemPadding = 15.thulurDp(),
    )
}
