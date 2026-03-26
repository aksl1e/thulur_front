package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainFeedViewModelTest {
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
    fun `emits empty when repository returns no threads`() = runTest {
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(FakeRepository(emptyList())),
        )

        advanceUntilIdle()

        assertEquals(MainFeedUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `emits success when repository returns threads`() = runTest {
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(
                FakeRepository(
                    listOf(
                        MainFeedThread(
                            id = "thread-1",
                            name = "Thread 1",
                            topicId = null,
                            topicName = null,
                            mainFeedScore = 0.8,
                            firstSeen = null,
                            summary = null,
                            articles = emptyList(),
                        ),
                    ),
                ),
            ),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState.Success(
                threads = listOf(
                    MainFeedThread(
                        id = "thread-1",
                        name = "Thread 1",
                        topicId = null,
                        topicName = null,
                        mainFeedScore = 0.8,
                        firstSeen = null,
                        summary = null,
                        articles = emptyList(),
                    ),
                ),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `emits error when repository throws`() = runTest {
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(ThrowingRepository()),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState.Error("Boom"),
            viewModel.uiState.value,
        )
    }
}

private class FakeRepository(
    private val threads: List<MainFeedThread>,
) : ThulurApiRepository {
    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> = threads
}

private class ThrowingRepository : ThulurApiRepository {
    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> {
        error("Boom")
    }
}
