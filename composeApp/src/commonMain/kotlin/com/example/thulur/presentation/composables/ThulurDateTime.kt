package com.example.thulur.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThulurDateTime(
    dateText: String?,
    timeText: String?,
    showDate: Boolean,
    modifier: Modifier = Modifier,
    dateColorOverride: Color? = null,
    timeColorOverride: Color? = null,
) {
    if (timeText == null) return

    val colors = ThulurTheme.SemanticColors.dateTime
    val typography = ThulurTheme.SemanticTypography
    val dateColor = dateColorOverride ?: colors.dateColor
    val timeColor = timeColorOverride ?: colors.timeColor

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.thulurDp()),
    ) {
        if (showDate && dateText != null) {
            BasicText(
                text = dateText,
                style = typography.dateTimeDate.copy(color = dateColor),
            )
        }

        BasicText(
            text = timeText,
            style = typography.dateTimeTime.copy(color = timeColor),
        )
    }
}
