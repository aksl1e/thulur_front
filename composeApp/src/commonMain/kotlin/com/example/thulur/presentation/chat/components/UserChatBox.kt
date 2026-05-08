package com.example.thulur.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun UserChatBox(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.chatScreen
    val typography = ThulurTheme.SemanticTypography

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.thulurDp(),
                        topEnd = 6.thulurDp(),
                        bottomStart = 18.thulurDp(),
                        bottomEnd = 18.thulurDp(),
                    )
                )
                .background(colors.userBubbleContainer)
                .padding(
                    horizontal = 16.thulurDp(),
                    vertical = 11.thulurDp(),
                ),
        ) {
            BasicText(
                text = message,
                style = typography.chatMarkdownBody.copy(
                    color = colors.userBubbleContent,
                ),
            )
        }
    }
}
