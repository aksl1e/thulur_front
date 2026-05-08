package com.example.thulur.presentation.chat.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun AiTypingChatBox(
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.chatScreen

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
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = colors.aiBubbleContent,
                modifier = Modifier.size(28.thulurDp()),
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
                    ),
                )
                .background(colors.aiBubbleContainer)
                .padding(
                    horizontal = 16.thulurDp(),
                    vertical = 16.thulurDp(),
                ),
        ) {
            TypingDots()
        }
    }
}

@Composable
private fun TypingDots() {
    val colors = ThulurTheme.SemanticColors.chatScreen
    val infiniteTransition = rememberInfiniteTransition(label = "assistant-typing")

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.thulurDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 550,
                        delayMillis = index * 140,
                        easing = FastOutSlowInEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "assistant-typing-dot-$index",
            )

            Box(
                modifier = Modifier
                    .size(8.thulurDp())
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(colors.aiBubbleContent),
            )
        }
    }
}
