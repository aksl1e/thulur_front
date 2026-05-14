package com.example.thulur.presentation.dailyfeed

import com.example.thulur.presentation.router.DailyFeedScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DailyFeedRouteKeyTest {
    @Test
    fun `generates stable key for session instance`() {
        assertEquals("daily-feed-session-1", DailyFeedScreen(sessionInstanceId = 1).key)
    }

    @Test
    fun `generates different keys for different sessions`() {
        assertNotEquals(
            DailyFeedScreen(sessionInstanceId = 1).key,
            DailyFeedScreen(sessionInstanceId = 2).key,
        )
    }
}
