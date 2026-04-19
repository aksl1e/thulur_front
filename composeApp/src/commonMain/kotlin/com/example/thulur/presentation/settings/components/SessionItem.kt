package com.example.thulur.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.settings.SettingsSessionState
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun SessionItem(
    session: SettingsSessionState,
    onTerminateClick: () -> Unit,
    isTerminating: Boolean,
    modifier: Modifier = Modifier,
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
            verticalArrangement = Arrangement.spacedBy(2.thulurDp()),
        ) {
            BasicText(
                text = session.title,
                style = typography.settingsSubsectionTitle.copy(
                    color = colors.bodyColor,
                ),
            )
            BasicText(
                text = session.clientLabel,
                style = typography.settingsBody.copy(
                    color = colors.bodyColor,
                ),
            )
            BasicText(
                text = session.metaLabel,
                style = typography.settingsMeta.copy(
                    color = colors.metaColor,
                ),
            )
        }

        ThulurButton(
            text = if (isTerminating) "Terminating..." else "Terminate",
            onClick = onTerminateClick,
            enabled = !isTerminating,
            colorRole = ThulurColorRole.Error,
            useContainerStates = false,
            stateColorsOverride = colors.terminateButton,
            textStyle = typography.settingsAction,
            contentPadding = PaddingValues(),
        )
    }
}
