package com.example.thulur.presentation.chat

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
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
import com.example.thulur.domain.model.DailyFeedThread
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
    threads: List<DailyFeedThread>,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(key = chatViewModelKey(sessionInstanceId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(threads) {
        viewModel.initWithThreads(threads)
    }
    ChatScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onThreadClick = viewModel::onThreadClick,
        onInputValueChange = viewModel::onInputValueChange,
        onSendClick = viewModel::onSendClick,
    )
}

internal fun chatViewModelKey(sessionInstanceId: Int): String =
    "chat-session-$sessionInstanceId"

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onBackClick: () -> Unit,
    onThreadClick: (DailyFeedThread) -> Unit,
    onInputValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    val colors = ThulurTheme.SemanticColors.settingsScreen
    val semanticTypography = ThulurTheme.SemanticTypography
    val leftRailWidth = 225.thulurDp()
    val contentPadding = 30.thulurDp()
    val contentStartPadding = 100.thulurDp()
    val contentEndPadding = 100.thulurDp()
    val contentBottomPadding = 15.thulurDp()
    // Local state for the input field
    //var inputValue by remember { mutableStateOf("") } // Do uiState

    // Each message is a Pair of (text, isUser) — true = user, false = AI
    // messeges is temporary will be deleted
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
            title = "Chat", // fallback title if no thread is selected
            backLabel = "Main Feed",
            onBackClick = onBackClick,
            // Shows the selected thread name in the center — falls back to title if null
            chatNameContent = if (selectedThreadName != null) {
                {
                    BasicText(
                        text = selectedThreadName,
                        modifier = Modifier.weight(1f, fill = false),
                        style = semanticTypography.appBarBackLabel.copy(
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
            // ---- Right content: chat messages + input bar ----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.screenBackground)
                    .padding(
                        start = contentStartPadding,
                        top = contentPadding,
                        end = contentEndPadding,
                        bottom = contentBottomPadding,
                    ),
            ) {
                // Message list — grows to fill available space above the input bar
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = contentPadding),
                        //.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.thulurDp()),
                    contentPadding = PaddingValues(vertical = contentPadding),
                ) {

                    items(uiState.messages) { message ->
                        if (message.isUser) {
                            UserChatBox(
                                message = message.text,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            AiChatBox(
                                message = message.text,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // Input bar — pinned to the bottom of the content area
                ChatInputSection(
                    value = uiState.inputValue,
                    onValueChange = onInputValueChange,
                    onSendClick = onSendClick,
                )
            }
        }
    }
}
@Preview
@Composable
private fun ChatScreenLightPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            ChatScreen(
                uiState = ChatUiState(),
                onBackClick = {},
                onThreadClick = {},
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
                uiState = ChatUiState(),
                onBackClick = {},
                onThreadClick = {},
                onInputValueChange = {},
                onSendClick = {},
            )
        }
    }
}