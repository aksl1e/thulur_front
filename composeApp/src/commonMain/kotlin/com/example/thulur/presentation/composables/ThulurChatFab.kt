package com.example.thulur.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDefaultChatCornerRadius
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThulurChatFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val colors = ThulurTheme.SemanticColors.chatFab
    val typography = ThulurTheme.SemanticTypography
    val shape = RoundedCornerShape(thulurDefaultChatCornerRadius())

    CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
        Row(
            modifier = modifier
                .wrapContentSize()
                .clip(shape)
                .background(colors.containerColor)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(20.thulurDp()),
            horizontalArrangement = Arrangement.spacedBy(10.thulurDp()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.invoke()
            BasicText(
                text = text,
                style = typography.chatFabLabel.copy(color = colors.contentColor),
            )
        }
    }
}

@Preview
@Composable
private fun ThulurChatFabPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            ThulurChatFab(
                text = "Action",
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
