package com.example.thulur.presentation.composables

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import kotlin.math.abs
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.desktopHorizontalWheelScroll(
    state: LazyListState,
): Modifier {
    val scope = rememberCoroutineScope()
    var isMoveActivated by remember { mutableStateOf(false) }
    val hasOverflow by remember(state) {
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

    LaunchedEffect(hasOverflow) {
        if (!hasOverflow) isMoveActivated = false
    }

    return this
        .onPointerEvent(PointerEventType.Move) {
            if (hasOverflow) {
                isMoveActivated = true
            }
        }
        .onPointerEvent(PointerEventType.Exit) {
            isMoveActivated = false
        }
        .onPointerEvent(PointerEventType.Scroll) { event ->
            if (!hasOverflow || !isMoveActivated) return@onPointerEvent

            val scrollDelta = event.changes.firstOrNull()?.scrollDelta ?: return@onPointerEvent
            val primaryAxisDelta = if (abs(scrollDelta.x) > abs(scrollDelta.y)) {
                scrollDelta.x
            } else {
                scrollDelta.y
            }

            if (primaryAxisDelta == 0f) return@onPointerEvent

            event.changes.forEach { it.consume() }
            scope.launch {
                val consumed = state.scrollBy(primaryAxisDelta * 40f)
                if (consumed == 0f) isMoveActivated = false
            }
        }
}
