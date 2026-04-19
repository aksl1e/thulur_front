package com.example.thulur.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.settings.SettingsSection
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun SettingsSectionSelector(
    selectedSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.thulurDp()),
    ) {
        SettingsSectionItem(
            label = "Account & App",
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                )
            },
            selected = selectedSection == SettingsSection.AccountAndApp,
            onClick = { onSectionSelected(SettingsSection.AccountAndApp) },
            modifier = Modifier.fillMaxWidth(),
        )
        SettingsSectionItem(
            label = "Subscription",
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                )
            },
            selected = false,
            enabled = false,
            onClick = { onSectionSelected(SettingsSection.Subscription) },
            modifier = Modifier.fillMaxWidth(),
        )
        SettingsSectionItem(
            label = "Feeds",
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.RssFeed,
                    contentDescription = null,
                )
            },
            selected = selectedSection == SettingsSection.Feeds,
            onClick = { onSectionSelected(SettingsSection.Feeds) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun SettingsSectionSelectorPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            SettingsSectionSelector(
                selectedSection = SettingsSection.AccountAndApp,
                onSectionSelected = {},
            )
        }
    }
}
