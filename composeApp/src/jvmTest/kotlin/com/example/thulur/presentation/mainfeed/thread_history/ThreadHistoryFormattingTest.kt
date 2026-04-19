package com.example.thulur.presentation.mainfeed.thread_history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class ThreadHistoryFormattingTest {
    @Test
    fun `formats day label as dd-slash-mmm-slash-yyyy`() {
        assertEquals(
            "15/Apr/2026",
            LocalDate(2026, 4, 15).toThreadHistoryDayLabel(),
        )
    }
}
