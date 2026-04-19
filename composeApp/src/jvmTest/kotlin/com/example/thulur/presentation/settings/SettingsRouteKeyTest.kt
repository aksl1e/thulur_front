package com.example.thulur.presentation.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SettingsRouteKeyTest {
    @Test
    fun `generates stable key for session instance`() {
        assertEquals("settings-session-1", settingsViewModelKey(sessionInstanceId = 1))
    }

    @Test
    fun `generates different keys for different sessions`() {
        assertNotEquals(
            settingsViewModelKey(sessionInstanceId = 1),
            settingsViewModelKey(sessionInstanceId = 2),
        )
    }
}
