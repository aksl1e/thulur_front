package com.example.thulur.presentation.composables

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import java.awt.event.MouseWheelEvent
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun Modifier.desktopHorizontalWheelScroll(
    state: LazyListState,
    coordinator: DesktopScrollCoordinator?,
    rowId: String?,
): Modifier {
    val scope = rememberCoroutineScope()
    val hasOverflow by rememberLazyListOverflow(state)

    return this.onPointerEvent(PointerEventType.Scroll) { event ->
        if (!hasOverflow) return@onPointerEvent

        val scrollDelta = event.primaryScrollDelta() ?: return@onPointerEvent
        val decision = resolveDesktopRowScrollDecision(
            scrollDelta = scrollDelta,
            hasOverflow = hasOverflow,
            coordinator = coordinator,
            rowId = rowId,
        )

        if (decision !is DesktopRowScrollDecision.Consume) return@onPointerEvent

        event.changes.forEach { it.consume() }
        scope.launch {
            state.scrollBy(decision.horizontalDelta * DESKTOP_SCROLL_MULTIPLIER)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun Modifier.desktopScrollRootObserver(
    coordinator: DesktopScrollCoordinator?,
): Modifier {
    if (coordinator == null) return this

    return this
        .onPointerEvent(PointerEventType.Move, PointerEventPass.Initial) { event ->
            val timestampMillis = event.awtEventOrNull?.`when` ?: return@onPointerEvent
            val position = event.changes.firstOrNull()?.position ?: return@onPointerEvent
            coordinator.onPointerMove(
                owner = DesktopScrollOwner.Column,
                timestampMillis = timestampMillis,
                position = position,
            )
        }
        .onPointerEvent(PointerEventType.Scroll, PointerEventPass.Initial) { event ->
            val scrollDelta = event.primaryScrollDelta() ?: return@onPointerEvent
            val timestampMillis = event.awtEventOrNull?.`when` ?: System.currentTimeMillis()
            val sessionKind = classifyDesktopScrollSessionKind(
                scrollDelta = scrollDelta,
                preciseWheelRotation = (event.awtEventOrNull as? MouseWheelEvent)?.preciseWheelRotation,
            )
            coordinator.beginOrContinueScrollSession(
                timestampMillis = timestampMillis,
                detectedKind = sessionKind,
            )
        }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun Modifier.desktopScrollRegionObserver(
    coordinator: DesktopScrollCoordinator?,
    owner: DesktopScrollOwner,
    pass: PointerEventPass,
): Modifier {
    if (coordinator == null) return this

    return this
        .onPointerEvent(PointerEventType.Move, pass) { event ->
            val timestampMillis = event.awtEventOrNull?.`when` ?: return@onPointerEvent
            val position = event.changes.firstOrNull()?.position ?: return@onPointerEvent
            coordinator.onPointerMove(
                owner = owner,
                timestampMillis = timestampMillis,
                position = position,
            )
        }
        .onPointerEvent(PointerEventType.Enter, pass) { event ->
            if (event.awtEventOrNull == null) return@onPointerEvent
            coordinator.onPointerEnter(owner)
        }
        .onPointerEvent(PointerEventType.Exit, pass) { event ->
            if (event.awtEventOrNull == null) return@onPointerEvent
            coordinator.onPointerExit(owner)
        }
}

@Composable
private fun rememberLazyListOverflow(state: LazyListState): androidx.compose.runtime.State<Boolean> = remember(state) {
    androidx.compose.runtime.derivedStateOf {
        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) {
            false
        } else {
            val lastVisibleItem = visibleItems.last()
            state.firstVisibleItemIndex > 0 ||
                state.firstVisibleItemScrollOffset > 0 ||
                lastVisibleItem.index < layoutInfo.totalItemsCount - 1 ||
                (lastVisibleItem.offset + lastVisibleItem.size) > layoutInfo.viewportEndOffset
        }
    }
}

internal fun classifyDesktopScrollSessionKind(
    scrollDelta: Offset,
    preciseWheelRotation: Double? = null,
): DesktopScrollSessionKind {
    if (abs(scrollDelta.x) > DESKTOP_SCROLL_DELTA_EPSILON) {
        return DesktopScrollSessionKind.TouchpadLike
    }

    if (preciseWheelRotation == null) {
        return DesktopScrollSessionKind.WheelLike
    }

    val nearestDiscreteStep = preciseWheelRotation.roundToInt().toDouble()
    val fractionalPart = abs(preciseWheelRotation - nearestDiscreteStep)
    return if (fractionalPart > DESKTOP_SCROLL_DELTA_EPSILON.toDouble()) {
        DesktopScrollSessionKind.TouchpadLike
    } else {
        DesktopScrollSessionKind.WheelLike
    }
}

private fun androidx.compose.ui.input.pointer.PointerEvent.primaryScrollDelta(): Offset? =
    changes.firstOrNull()?.scrollDelta?.takeUnless { it == Offset.Zero }
