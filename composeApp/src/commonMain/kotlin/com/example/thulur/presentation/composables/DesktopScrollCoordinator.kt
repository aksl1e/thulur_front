package com.example.thulur.presentation.composables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

internal enum class DesktopScrollSessionKind {
    WheelLike,
    TouchpadLike,
}

internal sealed interface DesktopScrollOwner {
    data object Column : DesktopScrollOwner
    data class Row(val rowId: String) : DesktopScrollOwner
}

internal sealed interface DesktopRowScrollDecision {
    data object PassToParent : DesktopRowScrollDecision
    data class Consume(
        val horizontalDelta: Float,
    ) : DesktopRowScrollDecision
}

internal class DesktopScrollCoordinator(
    private val sessionTimeoutMillis: Long = DESKTOP_SCROLL_SESSION_TIMEOUT_MILLIS,
    private val moveReacquireTimeoutMillis: Long = DESKTOP_SCROLL_MOVE_REACQUIRE_TIMEOUT_MILLIS,
    private val ownerChangeDistanceThreshold: Float = DESKTOP_SCROLL_OWNER_CHANGE_DISTANCE_THRESHOLD,
) {
    var pointerRegion: DesktopScrollOwner by mutableStateOf<DesktopScrollOwner>(DesktopScrollOwner.Column)
        private set

    var activeSessionKind: DesktopScrollSessionKind? by mutableStateOf(null)
        private set

    var activeWheelOwner: DesktopScrollOwner? by mutableStateOf(null)
        private set

    var lastScrollEventAtMillis by mutableLongStateOf(Long.MIN_VALUE)
        private set

    private var wheelIntentRegion: DesktopScrollOwner by mutableStateOf<DesktopScrollOwner>(DesktopScrollOwner.Column)
    private var lastPointerMoveAtMillis by mutableLongStateOf(Long.MIN_VALUE)
    private var hasPendingWheelOwnerRefresh by mutableStateOf(false)
    private var pendingOwnerCandidate: DesktopScrollOwner? by mutableStateOf(null)
    private var pendingOwnerCandidateDistance by mutableStateOf(0f)
    private var lastPointerPosition: Offset? by mutableStateOf(null)
    private var currentMoveTimestamp by mutableLongStateOf(Long.MIN_VALUE)
    private var currentMovePosition: Offset? by mutableStateOf(null)
    private var currentMoveDistance by mutableStateOf(0f)

    fun onPointerEnter(owner: DesktopScrollOwner) {
        pointerRegion = owner
    }

    fun onPointerExit(owner: DesktopScrollOwner) {
        if (pointerRegion == owner) {
            pointerRegion = DesktopScrollOwner.Column
        }
    }

    fun onPointerMove(
        owner: DesktopScrollOwner,
        timestampMillis: Long,
        position: Offset,
    ) {
        pointerRegion = owner

        val movementDistance = movementDistanceFor(
            timestampMillis = timestampMillis,
            position = position,
        )

        if (owner == wheelIntentRegion) {
            clearPendingOwnerCandidate()
            return
        }

        if (pendingOwnerCandidate != owner) {
            pendingOwnerCandidate = owner
            pendingOwnerCandidateDistance = movementDistance
        } else {
            pendingOwnerCandidateDistance += movementDistance
        }

        if (pendingOwnerCandidateDistance >= ownerChangeDistanceThreshold) {
            wheelIntentRegion = owner
            clearPendingOwnerCandidate()
            lastPointerMoveAtMillis = timestampMillis
            if (activeSessionKind == DesktopScrollSessionKind.WheelLike) {
                hasPendingWheelOwnerRefresh = activeWheelOwner != owner
            }
        }
    }

    fun beginOrContinueScrollSession(
        timestampMillis: Long,
        detectedKind: DesktopScrollSessionKind,
    ) {
        if (isNewSession(timestampMillis)) {
            activeSessionKind = detectedKind
            activeWheelOwner = if (detectedKind == DesktopScrollSessionKind.WheelLike) {
                wheelIntentRegion
            } else {
                null
            }
            hasPendingWheelOwnerRefresh = false
        } else if (
            detectedKind == DesktopScrollSessionKind.WheelLike &&
            activeSessionKind == DesktopScrollSessionKind.WheelLike &&
            hasPendingWheelOwnerRefresh &&
            lastPointerMoveAtMillis != Long.MIN_VALUE &&
            (timestampMillis - lastPointerMoveAtMillis) >= moveReacquireTimeoutMillis
        ) {
            activeWheelOwner = wheelIntentRegion
            hasPendingWheelOwnerRefresh = false
        }

        lastScrollEventAtMillis = timestampMillis
    }

    fun isNewSession(timestampMillis: Long): Boolean {
        val previousTimestamp = lastScrollEventAtMillis
        return activeSessionKind == null ||
            previousTimestamp == Long.MIN_VALUE ||
            (timestampMillis - previousTimestamp) > sessionTimeoutMillis
    }

    private fun clearPendingOwnerCandidate() {
        pendingOwnerCandidate = null
        pendingOwnerCandidateDistance = 0f
    }

    private fun movementDistanceFor(
        timestampMillis: Long,
        position: Offset,
    ): Float {
        val isSamePhysicalMove =
            timestampMillis == currentMoveTimestamp &&
                position == currentMovePosition

        if (isSamePhysicalMove) {
            return currentMoveDistance
        }

        val distance = lastPointerPosition?.let { previous ->
            euclideanDistance(previous, position)
        } ?: 0f

        currentMoveTimestamp = timestampMillis
        currentMovePosition = position
        currentMoveDistance = distance
        lastPointerPosition = position

        return distance
    }
}

internal fun resolveDesktopRowScrollDecision(
    scrollDelta: Offset,
    hasOverflow: Boolean,
    coordinator: DesktopScrollCoordinator? = null,
    rowId: String? = null,
    dominantAxisThreshold: Float = DESKTOP_SCROLL_DOMINANT_AXIS_THRESHOLD,
): DesktopRowScrollDecision {
    if (!hasOverflow) return DesktopRowScrollDecision.PassToParent

    if (coordinator == null || rowId == null) {
        val primaryAxisDelta = primaryScrollAxisDelta(scrollDelta)
        return if (primaryAxisDelta == 0f) {
            DesktopRowScrollDecision.PassToParent
        } else {
            DesktopRowScrollDecision.Consume(
                horizontalDelta = primaryAxisDelta,
            )
        }
    }

    return when (coordinator.activeSessionKind) {
        DesktopScrollSessionKind.TouchpadLike -> {
            val rowOwner = DesktopScrollOwner.Row(rowId)
            if (coordinator.pointerRegion != rowOwner ||
                !isHorizontalDominantScroll(scrollDelta, dominantAxisThreshold)
            ) {
                DesktopRowScrollDecision.PassToParent
            } else {
                DesktopRowScrollDecision.Consume(
                    horizontalDelta = scrollDelta.x,
                )
            }
        }

        DesktopScrollSessionKind.WheelLike -> {
            if (coordinator.activeWheelOwner != DesktopScrollOwner.Row(rowId)) {
                DesktopRowScrollDecision.PassToParent
            } else {
                val primaryAxisDelta = primaryScrollAxisDelta(scrollDelta)
                if (primaryAxisDelta == 0f) {
                    DesktopRowScrollDecision.PassToParent
                } else {
                    DesktopRowScrollDecision.Consume(
                        horizontalDelta = primaryAxisDelta,
                    )
                }
            }
        }

        null -> DesktopRowScrollDecision.PassToParent
    }
}

internal fun isHorizontalDominantScroll(
    scrollDelta: Offset,
    dominantAxisThreshold: Float = DESKTOP_SCROLL_DOMINANT_AXIS_THRESHOLD,
): Boolean {
    val horizontal = abs(scrollDelta.x)
    val vertical = abs(scrollDelta.y)

    return horizontal > DESKTOP_SCROLL_DELTA_EPSILON &&
        horizontal >= vertical * dominantAxisThreshold
}

internal fun primaryScrollAxisDelta(scrollDelta: Offset): Float = if (abs(scrollDelta.x) > abs(scrollDelta.y)) {
    scrollDelta.x
} else {
    scrollDelta.y
}

internal const val DESKTOP_SCROLL_SESSION_TIMEOUT_MILLIS: Long = 500L
internal const val DESKTOP_SCROLL_MOVE_REACQUIRE_TIMEOUT_MILLIS: Long = 150L
internal const val DESKTOP_SCROLL_OWNER_CHANGE_DISTANCE_THRESHOLD: Float = 16f
internal const val DESKTOP_SCROLL_DOMINANT_AXIS_THRESHOLD: Float = 1.25f
internal const val DESKTOP_SCROLL_DELTA_EPSILON: Float = 0.01f
internal const val DESKTOP_SCROLL_MULTIPLIER: Float = 40f

private fun euclideanDistance(
    first: Offset,
    second: Offset,
): Float {
    val dx = second.x - first.x
    val dy = second.y - first.y
    return kotlin.math.sqrt((dx * dx) + (dy * dy))
}
