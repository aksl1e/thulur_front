package com.example.thulur.presentation.composables

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DesktopScrollCoordinatorTest {
    @Test
    fun `wheel session keeps initial column ownership until timeout`() {
        val coordinator = DesktopScrollCoordinator()

        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 60L,
            position = Offset.Zero,
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Row("thread-1"),
            timestampMillis = 120L,
            position = Offset(30f, 0f),
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 200L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollSessionKind.WheelLike, coordinator.activeSessionKind)
        assertEquals(DesktopScrollOwner.Column, coordinator.activeWheelOwner)
        assertEquals(DesktopScrollOwner.Row("thread-1"), coordinator.pointerRegion)
    }

    @Test
    fun `new wheel session after moving to row selects row ownership`() {
        val coordinator = DesktopScrollCoordinator()

        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 40L,
            position = Offset.Zero,
        )
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Row("thread-1"),
            timestampMillis = 80L,
            position = Offset(24f, 0f),
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollOwner.Row("thread-1"), coordinator.activeWheelOwner)
    }

    @Test
    fun `touchpad horizontal dominant gesture scrolls hovered row`() {
        val coordinator = DesktopScrollCoordinator()
        coordinator.onPointerEnter(DesktopScrollOwner.Row("thread-1"))
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.TouchpadLike,
        )

        val decision = resolveDesktopRowScrollDecision(
            scrollDelta = Offset(5f, 3f),
            hasOverflow = true,
            coordinator = coordinator,
            rowId = "thread-1",
        )

        val consume = assertIs<DesktopRowScrollDecision.Consume>(decision)
        assertEquals(5f, consume.horizontalDelta)
    }

    @Test
    fun `wheel session started in row switches to column only after real move outside row`() {
        val coordinator = DesktopScrollCoordinator()
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 40L,
            position = Offset.Zero,
        )
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Row("thread-1"),
            timestampMillis = 80L,
            position = Offset(24f, 0f),
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 140L,
            position = Offset(28f, 0f),
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 200L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollOwner.Row("thread-1"), coordinator.activeWheelOwner)

        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 230L,
            position = Offset(52f, 0f),
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 400L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollOwner.Column, coordinator.activeWheelOwner)
        assertEquals(DesktopScrollOwner.Column, coordinator.pointerRegion)
    }

    @Test
    fun `wheel session can reacquire row after real move and short pause`() {
        val coordinator = DesktopScrollCoordinator()

        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 60L,
            position = Offset.Zero,
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Row("thread-1"),
            timestampMillis = 140L,
            position = Offset(24f, 0f),
        )

        coordinator.beginOrContinueScrollSession(
            timestampMillis = 220L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollOwner.Column, coordinator.activeWheelOwner)

        coordinator.beginOrContinueScrollSession(
            timestampMillis = 300L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollOwner.Row("thread-1"), coordinator.activeWheelOwner)
    }

    @Test
    fun `small move into row does not switch wheel owner`() {
        val coordinator = DesktopScrollCoordinator()

        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Column,
            timestampMillis = 80L,
            position = Offset.Zero,
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Row("thread-1"),
            timestampMillis = 140L,
            position = Offset(8f, 0f),
        )

        coordinator.beginOrContinueScrollSession(
            timestampMillis = 320L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        assertEquals(DesktopScrollOwner.Column, coordinator.activeWheelOwner)
    }

    @Test
    fun `touchpad vertical gesture passes through hovered row`() {
        val coordinator = DesktopScrollCoordinator()
        coordinator.onPointerEnter(DesktopScrollOwner.Row("thread-1"))
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.TouchpadLike,
        )

        val decision = resolveDesktopRowScrollDecision(
            scrollDelta = Offset(1f, 4f),
            hasOverflow = true,
            coordinator = coordinator,
            rowId = "thread-1",
        )

        assertEquals(DesktopRowScrollDecision.PassToParent, decision)
    }

    @Test
    fun `touchpad near tie prefers column`() {
        val coordinator = DesktopScrollCoordinator()
        coordinator.onPointerEnter(DesktopScrollOwner.Row("thread-1"))
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.TouchpadLike,
        )

        val decision = resolveDesktopRowScrollDecision(
            scrollDelta = Offset(5f, 4.5f),
            hasOverflow = true,
            coordinator = coordinator,
            rowId = "thread-1",
        )

        assertEquals(DesktopRowScrollDecision.PassToParent, decision)
    }

    @Test
    fun `row without overflow never consumes scroll`() {
        val coordinator = DesktopScrollCoordinator()
        coordinator.onPointerMove(
            owner = DesktopScrollOwner.Row("thread-1"),
            timestampMillis = 80L,
            position = Offset(24f, 0f),
        )
        coordinator.beginOrContinueScrollSession(
            timestampMillis = 100L,
            detectedKind = DesktopScrollSessionKind.WheelLike,
        )

        val decision = resolveDesktopRowScrollDecision(
            scrollDelta = Offset(0f, 4f),
            hasOverflow = false,
            coordinator = coordinator,
            rowId = "thread-1",
        )

        assertEquals(DesktopRowScrollDecision.PassToParent, decision)
    }
}
