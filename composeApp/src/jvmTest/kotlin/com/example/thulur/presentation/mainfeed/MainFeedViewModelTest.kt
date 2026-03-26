package com.example.thulur.presentation.mainfeed

import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import com.example.thulur.presentation.composables.TopicsViewMode
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MainFeedViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

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
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(emptyList()),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState(
                selectedDay = today,
                contentState = MainFeedContentState.Empty,
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `emits success when repository returns threads`() = runTest {
        val threads = listOf(sampleThread())
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(threads),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState(
                selectedDay = today,
                contentState = MainFeedContentState.Success(threads),
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `emits error when repository throws`() = runTest {
        val repository = TrackingRepository(
            result = Result.failure<List<MainFeedThread>>(IllegalStateException("Boom")),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(
            MainFeedUiState(
                selectedDay = today,
                contentState = MainFeedContentState.Error("Boom"),
            ),
            viewModel.uiState.value,
        )
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `back click changes selected day and reloads feed`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        assertEquals(yesterday, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today, yesterday), repository.requestedDays)
    }

    @Test
    fun `forward click changes selected day toward today and reloads feed`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()

        assertEquals(today, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today, today.minus(1, DateTimeUnit.DAY), today), repository.requestedDays)
    }

    @Test
    fun `forward click on today does nothing`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()

        assertEquals(today, viewModel.uiState.value.selectedDay)
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }

    @Test
    fun `retry reloads the currently selected day`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        assertEquals(listOf<LocalDate?>(today, yesterday, yesterday), repository.requestedDays)
    }

    @Test
    fun `retry reloads the currently selected day after forward navigation`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onBackClick()
        advanceUntilIdle()
        viewModel.onForwardClick()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(listOf<LocalDate?>(today, today.minus(1, DateTimeUnit.DAY), today, today), repository.requestedDays)
    }

    @Test
    fun `topics mode updates locally without reloading data`() = runTest {
        val repository = TrackingRepository(
            result = Result.success<List<MainFeedThread>>(listOf(sampleThread())),
        )
        val viewModel = MainFeedViewModel(
            getMainFeedUseCase = GetMainFeedUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onTopicsViewModeChange(TopicsViewMode.TopicsOnly)

        assertEquals(TopicsViewMode.TopicsOnly, viewModel.uiState.value.topicsViewMode)
        assertEquals(listOf<LocalDate?>(today), repository.requestedDays)
    }
}

private class TrackingRepository(
    private val result: Result<List<MainFeedThread>>,
) : ThulurApiRepository {
    val requestedDays = mutableListOf<LocalDate?>()

    override suspend fun getMainFeed(day: LocalDate?): List<MainFeedThread> {
        requestedDays += day
        return result.getOrThrow()
    }
}

private fun sampleThread() = MainFeedThread(
    id = "thread-1",
    name = "Thread 1",
    topicId = null,
    topicName = null,
    mainFeedScore = 0.8,
    firstSeen = null,
    summary = null,
    articles = emptyList(),
)
