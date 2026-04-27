package com.example.thulur.presentation.dailyfeed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DailyFeedRouteKeyTest {
    @Test
    fun `generates stable key for session instance`() {
        assertEquals("daily-feed-session-1", dailyFeedViewModelKey(sessionInstanceId = 1))
    }

    @Test
    fun `generates different keys for different sessions`() {
        assertNotEquals(
            dailyFeedViewModelKey(sessionInstanceId = 1),
            dailyFeedViewModelKey(sessionInstanceId = 2),
        )
    }
}
