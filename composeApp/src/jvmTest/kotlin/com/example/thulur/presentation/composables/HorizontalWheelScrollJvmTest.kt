package com.example.thulur.presentation.composables

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals

class HorizontalWheelScrollJvmTest {
    @Test
    fun `horizontal scroll delta is treated as touchpad-like`() {
        assertEquals(
            DesktopScrollSessionKind.TouchpadLike,
            classifyDesktopScrollSessionKind(
                scrollDelta = Offset(1f, 0f),
                preciseWheelRotation = 1.0,
            ),
        )
    }

    @Test
    fun `fractional wheel rotation is treated as touchpad-like`() {
        assertEquals(
            DesktopScrollSessionKind.TouchpadLike,
            classifyDesktopScrollSessionKind(
                scrollDelta = Offset.Zero,
                preciseWheelRotation = 0.25,
            ),
        )
    }

    @Test
    fun `discrete wheel rotation is treated as wheel-like`() {
        assertEquals(
            DesktopScrollSessionKind.WheelLike,
            classifyDesktopScrollSessionKind(
                scrollDelta = Offset.Zero,
                preciseWheelRotation = 1.0,
            ),
        )
    }
}
