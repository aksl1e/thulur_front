package com.example.thulur.presentation.chat.components

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.thulur.presentation.composables.ThulurButton
import com.example.thulur.presentation.composables.ThulurTextField
import com.example.thulur.presentation.theme.ThulurColorRole
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

/**
 * Chat input aligned with Thulur semantic design system.
 *
 * Because ThulurTextField has no trailing slot,
 * send button is overlaid on the right side.
 */
@Composable
fun ChatInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ThulurTheme.SemanticColors.chatScreen

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.thulurDp(),
                vertical = 12.thulurDp(),
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,  // changed from CenterVertically so button stays at bottom
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()  // removed fixed height so it expands with content
                .wrapContentHeight()
                .heightIn(min = 46.thulurDp(), max = 160.thulurDp()),
        ) {

            ThulurTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "Ask Chatbot",
                enabled = enabled,
                singleLine = false,  // allows multiline
                modifier = Modifier.fillMaxWidth(),
                stateColorsOverride = colors.inputField,
                shape = RoundedCornerShape(20.thulurDp()),
                contentPadding = PaddingValues(
                    start = 20.thulurDp(),
                    end = 56.thulurDp(),  // increased to avoid text running under send button
                    top = 12.thulurDp(),
                    bottom = 12.thulurDp(),
                ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)  // changed from CenterEnd so it stays at bottom as field grows
                    .padding(end = 12.thulurDp(), bottom = 5.thulurDp()),
            ) {
                ThulurButton(
                    onClick = onSendClick,
                    enabled = enabled && value.isNotBlank(),
                    colorRole = ThulurColorRole.Primary,
                    useContainerStates = false,
                    stateColorsOverride = colors.sendButton,
                    contentPadding = PaddingValues(0.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.height(36.thulurDp()),
                        )
                    },
                )
            }
        }
    }
}