package com.example.thulur.presentation.dailyfeed.thread_history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.datetime.LocalDate

class ThreadHistoryRouteKeyTest {
    @Test
    fun `generates stable key for same session thread and initial day`() {
        assertEquals(
            "thread-history-session-1-thread-thread-1-day-2026-04-14",
            threadHistoryViewModelKey(
                sessionInstanceId = 1,
                threadId = "thread-1",
                initialDay = LocalDate(2026, 4, 14),
            ),
        )
    }

    @Test
    fun `generates different keys for different initial day`() {
        assertNotEquals(
            threadHistoryViewModelKey(
                sessionInstanceId = 1,
                threadId = "thread-1",
                initialDay = LocalDate(2026, 4, 14),
            ),
            threadHistoryViewModelKey(
                sessionInstanceId = 1,
                threadId = "thread-1",
                initialDay = LocalDate(2026, 4, 15),
            ),
        )
    }
}
