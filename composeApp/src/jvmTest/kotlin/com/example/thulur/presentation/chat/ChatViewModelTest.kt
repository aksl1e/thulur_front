package com.example.thulur.presentation.chat

import com.example.thulur.domain.model.ArticleParagraph
import com.example.thulur.domain.model.AuthSession
import com.example.thulur.domain.model.CurrentUser
import com.example.thulur.domain.model.DailyFeed
import com.example.thulur.domain.model.Feed
import com.example.thulur.domain.model.PatchUserSettings
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.UserSettings
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.SendGeneralChatMessageUseCase
import com.example.thulur.domain.usecase.SendThreadChatMessageUseCase
import com.example.thulur.presentation.dailyfeed.OpenChat
import com.example.thulur.presentation.dailyfeed.OpenChatMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `general mode sends message and replaces pending assistant`() = runTest {
        val repository = TrackingChatRepository()
        val viewModel = createViewModel(repository)
        val response = CompletableDeferred<String>()
        repository.generalResponder = { response.await() }

        viewModel.initialize(
            OpenChat(
                openId = 1,
                title = "Today",
                mode = OpenChatMode.General,
            ),
        )
        viewModel.onInputValueChange("  Hello there  ")
        viewModel.onSendClick()
        runCurrent()

        assertEquals(listOf("Hello there"), repository.generalMessages)
        assertEquals(
            listOf(
                ChatMessage.User("Hello there"),
                ChatMessage.AssistantPending,
            ),
            viewModel.uiState.value.messages,
        )
        assertEquals(true, viewModel.uiState.value.isSending)
        assertEquals("", viewModel.uiState.value.inputValue)

        response.complete("**Hi** back")
        advanceUntilIdle()

        assertEquals(
            listOf(
                ChatMessage.User("Hello there"),
                ChatMessage.Assistant(markdown = "**Hi** back"),
            ),
            viewModel.uiState.value.messages,
        )
        assertEquals(false, viewModel.uiState.value.isSending)
    }

    @Test
    fun `thread mode sends thread scoped message`() = runTest {
        val repository = TrackingChatRepository()
        val viewModel = createViewModel(repository)
        val response = CompletableDeferred<String>()
        repository.threadResponder = { _, _ -> response.await() }

        viewModel.initialize(
            OpenChat(
                openId = 2,
                title = "Thread 1",
                mode = OpenChatMode.Thread(threadId = "thread-1"),
            ),
        )
        viewModel.onInputValueChange("Question")
        viewModel.onSendClick()
        runCurrent()

        assertEquals(listOf("thread-1" to "Question"), repository.threadMessages)

        response.complete("Thread answer")
        advanceUntilIdle()

        assertEquals(
            listOf(
                ChatMessage.User("Question"),
                ChatMessage.Assistant(markdown = "Thread answer"),
            ),
            viewModel.uiState.value.messages,
        )
    }

    @Test
    fun `while sending additional input and send attempts are ignored`() = runTest {
        val repository = TrackingChatRepository()
        val viewModel = createViewModel(repository)
        val response = CompletableDeferred<String>()
        repository.generalResponder = { response.await() }

        viewModel.initialize(
            OpenChat(
                openId = 3,
                title = "Today",
                mode = OpenChatMode.General,
            ),
        )
        viewModel.onInputValueChange("First")
        viewModel.onSendClick()
        runCurrent()
        viewModel.onInputValueChange("Second")
        viewModel.onSendClick()

        assertEquals(listOf("First"), repository.generalMessages)
        assertEquals("", viewModel.uiState.value.inputValue)
        assertEquals(
            listOf(
                ChatMessage.User("First"),
                ChatMessage.AssistantPending,
            ),
            viewModel.uiState.value.messages,
        )

        response.complete("Done")
        advanceUntilIdle()
    }

    @Test
    fun `errors replace pending assistant bubble`() = runTest {
        val repository = TrackingChatRepository()
        val viewModel = createViewModel(repository)
        repository.generalResponder = { throw IllegalStateException("Boom") }

        viewModel.initialize(
            OpenChat(
                openId = 4,
                title = "Today",
                mode = OpenChatMode.General,
            ),
        )
        viewModel.onInputValueChange("First")
        viewModel.onSendClick()
        advanceUntilIdle()

        assertEquals(
            listOf(
                ChatMessage.User("First"),
                ChatMessage.Assistant(markdown = "Boom", isError = true),
            ),
            viewModel.uiState.value.messages,
        )
        assertEquals(false, viewModel.uiState.value.isSending)
    }

    @Test
    fun `new open id reinitializes chat session state`() = runTest {
        val repository = TrackingChatRepository()
        val viewModel = createViewModel(repository)

        viewModel.initialize(
            OpenChat(
                openId = 5,
                title = "Today",
                mode = OpenChatMode.General,
            ),
        )
        viewModel.onInputValueChange("Draft")

        viewModel.initialize(
            OpenChat(
                openId = 6,
                title = "Thread 1",
                mode = OpenChatMode.Thread(threadId = "thread-1"),
            ),
        )

        assertEquals(
            ChatUiState(
                title = "Thread 1",
                mode = OpenChatMode.Thread(threadId = "thread-1"),
            ),
            viewModel.uiState.value,
        )
    }
}

private fun createViewModel(repository: TrackingChatRepository): ChatViewModel = ChatViewModel(
    sendGeneralChatMessageUseCase = SendGeneralChatMessageUseCase(repository),
    sendThreadChatMessageUseCase = SendThreadChatMessageUseCase(repository),
)

private class TrackingChatRepository : ThulurApiRepository {
    val generalMessages = mutableListOf<String>()
    val threadMessages = mutableListOf<Pair<String, String>>()
    var generalResponder: suspend (String) -> String = { "ok" }
    var threadResponder: suspend (String, String) -> String = { _, _ -> "ok" }

    override suspend fun sendGeneralChatMessage(message: String): String {
        generalMessages += message
        return generalResponder(message)
    }

    override suspend fun sendThreadChatMessage(threadId: String, message: String): String {
        threadMessages += threadId to message
        return threadResponder(threadId, message)
    }

    override suspend fun getDailyFeed(day: LocalDate?): DailyFeed = error("Not used")
    override suspend fun getArticleParagraphs(articleId: String): List<ArticleParagraph> = error("Not used")
    override suspend fun getUserSettings(): UserSettings = error("Not used")
    override suspend fun patchUserSettings(patch: PatchUserSettings): UserSettings = error("Not used")
    override suspend fun getFollowedFeeds(): List<Feed> = error("Not used")
    override suspend fun getAllFeeds(): List<Feed> = error("Not used")
    override suspend fun followFeed(identifier: String): Unit = error("Not used")
    override suspend fun unfollowFeed(feedId: String): Unit = error("Not used")
    override suspend fun getCurrentUser(): CurrentUser = error("Not used")
    override suspend fun getAuthSessions(): List<AuthSession> = error("Not used")
    override suspend fun terminateAuthSession(sessionId: String): Unit = error("Not used")
    override suspend fun getThreadHistory(threadId: String): ThreadHistory = error("Not used")
    override suspend fun rateArticle(articleId: String, rating: Int): Unit = error("Not used")
}
