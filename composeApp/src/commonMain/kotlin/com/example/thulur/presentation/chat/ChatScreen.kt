package com.example.thulur.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.presentation.chat.components.AiChatBox
import com.example.thulur.presentation.chat.components.ChatInputSection
import com.example.thulur.presentation.chat.components.ThreadSectionSelector
import com.example.thulur.presentation.chat.components.UserChatBox
import com.example.thulur.presentation.composables.ThulurAppBar
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatRoute(
    sessionInstanceId: Int,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(key = chatViewModelKey(sessionInstanceId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onThreadClick = viewModel::onThreadClick,
    )
}

internal fun chatViewModelKey(sessionInstanceId: Int): String =
    "chat-session-$sessionInstanceId"

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onBackClick: () -> Unit,
    onThreadClick: (MainFeedThread) -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val leftRailWidth = 225.thulurDp()
    val contentPadding = 30.thulurDp()

    // Local state for the input field
    var inputValue by remember { mutableStateOf("") }

    // Each message is a Pair of (text, isUser) — true = user, false = AI
    val messages = remember {
        mutableStateListOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit." to true,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." to false,
        )
    }

    val listState = rememberLazyListState()

    // Resolve the selected thread name from the success state for the AppBar title
    val selectedThreadName = (uiState.contentState as? ChatContentState.Success)
        ?.threads
        ?.firstOrNull { it.id == uiState.selectedThreadId }
        ?.name

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBackground)
            .safeContentPadding(),
    ) {
        ThulurAppBar(
            title = "Discuss", // fallback title if no thread is selected
            backLabel = "Main Feed",
            onBackClick = onBackClick,
            // Shows the selected thread name in the center — falls back to title if null
            chatNameContent = if (selectedThreadName != null) {
                {
                    BasicText(
                        text = selectedThreadName,
                        modifier = Modifier.weight(1f, fill = false),
                        style = ThulurTheme.SemanticTypography.appBarBackLabel.copy(
                            color = ThulurTheme.SemanticColors.appBar.titleColor,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                null
            },
        )

        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            // ---- Left rail: thread selector ----
            Box(
                modifier = Modifier
                    .width(leftRailWidth)
                    .fillMaxHeight()
                    .background(colors.railColor),
            ) {
                when (val contentState = uiState.contentState) {
                    ChatContentState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.padding(contentPadding),
                    )

                    ChatContentState.Empty -> BasicText(
                        text = "No threads available.",
                        modifier = Modifier.padding(contentPadding),
                    )

                    is ChatContentState.Error -> BasicText(
                        text = contentState.message,
                        modifier = Modifier.padding(contentPadding),
                    )

                    is ChatContentState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            // "Chats" label above the thread list — matches reference design
                            BasicText(
                                text = "Chats",
                                modifier = Modifier.padding(
                                    start = 16.thulurDp(),
                                    top = contentPadding,
                                    bottom = 10.thulurDp(),
                                ),
                                style = ThulurTheme.SemanticTypography.settingsSubsectionTitle.copy(
                                    color = ThulurTheme.SemanticColors.chatScreen.chatsLabelColor,
                                ),
                            )

                            ThreadSectionSelector(
                                threads = contentState.threads,
                                selectedThreadId = uiState.selectedThreadId,
                                onThreadSelected = onThreadClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            // ---- Right content: chat messages + input bar ----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.screenBackground),
            ) {
                // Message list — grows to fill available space above the input bar
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = contentPadding),
                    verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
                    contentPadding = PaddingValues(vertical = contentPadding),
                ) {
                    items(messages) { (text, isUser) ->
                        if (isUser) {
                            // User message bubble
                            UserChatBox(
                                message = text,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            // AI message bubble
                            AiChatBox(
                                message = text,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // Input bar — pinned to the bottom of the content area
                ChatInputSection(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    onSendClick = {
                        if (inputValue.isNotBlank()) {
                            messages.add(inputValue to true)
                            inputValue = ""
                        }
                    },
                )
            }
        }
    }
}