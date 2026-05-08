package com.example.thulur.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.presentation.chat.components.AiChatBox
import com.example.thulur.presentation.chat.components.AiTypingChatBox
import com.example.thulur.presentation.chat.components.ChatInputSection
import com.example.thulur.presentation.chat.components.UserChatBox
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.dailyfeed.OpenChat
import com.example.thulur.presentation.dailyfeed.OpenChatMode
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatRoute(
    sessionInstanceId: Int,
    openChat: OpenChat,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(
        key = chatViewModelKey(
            sessionInstanceId = sessionInstanceId,
            openId = openChat.openId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(openChat.openId) {
        viewModel.initialize(openChat)
    }

    ChatScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onInputValueChange = viewModel::onInputValueChange,
        onSendClick = viewModel::onSendClick,
    )
}

internal fun chatViewModelKey(sessionInstanceId: Int, openId: Int): String =
    "chat-session-$sessionInstanceId-open-$openId"

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onBackClick: () -> Unit,
    onInputValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.chatScreen
    val semanticTypography = ThulurTheme.SemanticTypography
    val contentPadding = 30.thulurDp()
    val contentHorizontalPadding = 100.thulurDp()
    val contentBottomPadding = 10.thulurDp()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(index = uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.contentBackground)
            .safeContentPadding(),
    ) {
        ThulurAppBar(
            title = "Chat",
            backLabel = when (uiState.mode) {
                OpenChatMode.General -> "Main Feed"
                is OpenChatMode.Thread -> "Back"
            },
            onBackClick = onBackClick,
            chatNameContent = {
                BasicText(
                    text = uiState.title,
                    style = semanticTypography.chatAppBarContext.copy(
                        color = ThulurTheme.SemanticColors.appBar.titleColor,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.contentBackground)
                .padding(
                    start = contentHorizontalPadding,
                    top = contentPadding,
                    end = contentHorizontalPadding,
                    bottom = contentBottomPadding,
                ),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = contentPadding),
                verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
                contentPadding = PaddingValues(vertical = contentPadding),
            ) {
                itemsIndexed(uiState.messages) { _, message ->
                    when (message) {
                        is ChatMessage.User -> UserChatBox(
                            message = message.text,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        is ChatMessage.Assistant -> AiChatBox(
                            markdown = message.markdown,
                            isError = message.isError,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        ChatMessage.AssistantPending -> AiTypingChatBox(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            ChatInputSection(
                value = uiState.inputValue,
                onValueChange = onInputValueChange,
                onSendClick = onSendClick,
                enabled = !uiState.isSending,
            )
        }
    }
}

@Preview
@Composable
private fun ChatScreenLightPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            ChatScreen(
                uiState = previewChatUiState(),
                onBackClick = {},
                onInputValueChange = {},
                onSendClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChatScreenDarkPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Dark) {
            ChatScreen(
                uiState = previewChatUiState(),
                onBackClick = {},
                onInputValueChange = {},
                onSendClick = {},
            )
        }
    }
}

private fun previewChatUiState(): ChatUiState = ChatUiState(
    title = "Today",
    messages = listOf(
        ChatMessage.User("What changed in the market today?"),
        ChatMessage.Assistant(
            markdown = "**Three things stood out**:\n- Energy cooled off\n- AI names stayed firm\n- Europe traded mixed",
        ),
    ),
)
