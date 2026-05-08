package com.example.thulur.presentation.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ChatRouteKeyTest {
    @Test
    fun `generates stable key for same session and open id`() {
        assertEquals(
            "chat-session-7-open-3",
            chatViewModelKey(sessionInstanceId = 7, openId = 3),
        )
    }

    @Test
    fun `generates different keys for different openings`() {
        assertNotEquals(
            chatViewModelKey(sessionInstanceId = 7, openId = 3),
            chatViewModelKey(sessionInstanceId = 7, openId = 4),
        )
    }
}
