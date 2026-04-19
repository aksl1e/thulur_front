package com.example.thulur.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.BasicText
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun AppSettingItem(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: @Composable () -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val typography = ThulurTheme.SemanticTypography

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.thulurDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.thulurDp()),
        ) {
            BasicText(
                text = title,
                style = typography.settingsSubsectionTitle.copy(
                    color = colors.bodyColor,
                ),
            )

            supportingText?.let {
                BasicText(
                    text = it,
                    style = typography.settingsBody.copy(
                        color = colors.bodyMutedColor,
                    ),
                )
            }
        }

        action()
    }
}
