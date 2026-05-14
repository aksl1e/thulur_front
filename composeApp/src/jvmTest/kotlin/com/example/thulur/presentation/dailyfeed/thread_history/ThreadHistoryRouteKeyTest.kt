package com.example.thulur.presentation.dailyfeed.thread_history

import com.example.thulur.presentation.router.ThreadHistoryScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.datetime.LocalDate

class ThreadHistoryRouteKeyTest {
    @Test
    fun `generates stable key for same session thread and initial day`() {
        assertEquals(
            "thread-history-session-1-thread-thread-1-day-2026-04-14",
            ThreadHistoryScreen(
                sessionInstanceId = 1,
                threadId = "thread-1",
                threadName = "Thread 1",
                initialDay = LocalDate(2026, 4, 14),
            ).key,
        )
    }

    @Test
    fun `generates different keys for different initial day`() {
        assertNotEquals(
            ThreadHistoryScreen(
                sessionInstanceId = 1,
                threadId = "thread-1",
                threadName = "Thread 1",
                initialDay = LocalDate(2026, 4, 14),
            ).key,
            ThreadHistoryScreen(
                sessionInstanceId = 1,
                threadId = "thread-1",
                threadName = "Thread 1",
                initialDay = LocalDate(2026, 4, 15),
            ).key,
        )
    }
}
