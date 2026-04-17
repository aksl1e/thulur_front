package com.example.thulur.presentation.mainfeed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MainFeedRouteKeyTest {
    @Test
    fun `generates stable key for session instance`() {
        assertEquals("main-feed-session-1", mainFeedViewModelKey(sessionInstanceId = 1))
    }

    @Test
    fun `generates different keys for different sessions`() {
        assertNotEquals(
            mainFeedViewModelKey(sessionInstanceId = 1),
            mainFeedViewModelKey(sessionInstanceId = 2),
        )
    }
}
