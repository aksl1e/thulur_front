package com.example.thulur.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThulurDateTime(
    dateText: String?,
    timeText: String?,
    showDate: Boolean,
    modifier: Modifier = Modifier,
) {
    if (timeText == null) return

    val colors = ThulurTheme.SemanticColors.dateTime
    val typography = ThulurTheme.SemanticTypography

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.thulurDp()),
    ) {
        if (showDate && dateText != null) {
            BasicText(
                text = dateText,
                style = typography.dateTimeDate.copy(color = colors.dateColor),
            )
        }

        BasicText(
            text = timeText,
            style = typography.dateTimeTime.copy(color = colors.timeColor),
        )
    }
}
