package com.example.thulur.presentation.chat

import com.example.thulur.presentation.router.ChatMode
import com.example.thulur.presentation.router.ChatScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ChatRouteKeyTest {
    @Test
    fun `generates stable key for same session and open id`() {
        assertEquals(
            "chat-session-7-open-3",
            ChatScreen(
                sessionInstanceId = 7,
                openId = 3,
                title = "Today",
                mode = ChatMode.General,
            ).key,
        )
    }

    @Test
    fun `generates different keys for different openings`() {
        assertNotEquals(
            ChatScreen(
                sessionInstanceId = 7,
                openId = 3,
                title = "Today",
                mode = ChatMode.General,
            ).key,
            ChatScreen(
                sessionInstanceId = 7,
                openId = 4,
                title = "Today",
                mode = ChatMode.General,
            ).key,
        )
    }
}
