package com.example.thulur.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.thulurDp()),
        ) {

            ThulurTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "Ask Chatbot",
                enabled = enabled,
                modifier = Modifier.matchParentSize(),
                stateColorsOverride = colors.inputField.copy(
                    rest = colors.inputField.rest.copy(
                        borderColor = colors.inputField.rest.borderColor.copy(alpha = 0f),
                        containerColor = colors.inputField.rest.containerColor.copy(alpha = 0.96f),
                    ),
                    focused = colors.inputField.focused.copy(
                        borderColor = colors.inputField.focused.borderColor.copy(alpha = 0f),
                        containerColor = colors.inputField.focused.containerColor.copy(alpha = 1f),
                    ),
                    error = colors.inputField.error.copy(
                        borderColor = colors.inputField.error.borderColor.copy(alpha = 0f),
                    ),
                    disabled = colors.inputField.disabled.copy(
                        borderColor = colors.inputField.disabled.borderColor.copy(alpha = 0f),
                    ),
                ),

                // miejsce na ikonę send po prawej
                contentPadding = PaddingValues(
                    start = 15.thulurDp(),
                    end = 44.thulurDp(),
                ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.thulurDp()),
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
                            imageVector = Icons.Outlined.Send,
                            contentDescription = "Send",
                            modifier = Modifier.height(18.thulurDp()),
                        )
                    },
                )
            }
        }
    }
}