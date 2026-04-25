package com.example.thulur.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun AiChatBox(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.chatScreen
    val typography = ThulurTheme.SemanticTypography

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.thulurDp()),
        verticalAlignment = Alignment.Top,
    ) {

        Box(
            modifier = Modifier
                .size(36.thulurDp())
                .clip(RoundedCornerShape(8.thulurDp())),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = colors.aiBubbleContent,
                modifier = Modifier.size(18.thulurDp()),
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 6.thulurDp(),
                        topEnd = 18.thulurDp(),
                        bottomStart = 18.thulurDp(),
                        bottomEnd = 18.thulurDp(),
                    )
                )
                .padding(
                    horizontal = 16.thulurDp(),
                    vertical = 11.thulurDp(),
                ),
        ) {
            BasicText(
                text = message,
                style = typography.settingsBody.copy(
                    color = colors.aiBubbleContent,
                ),
            )
        }
    }
}