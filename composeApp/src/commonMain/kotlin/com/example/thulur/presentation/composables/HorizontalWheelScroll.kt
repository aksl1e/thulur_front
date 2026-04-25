package com.example.thulur.presentation.composables

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass

@Composable
internal expect fun Modifier.desktopHorizontalWheelScroll(
    state: LazyListState,
    coordinator: DesktopScrollCoordinator? = null,
    rowId: String? = null,
): Modifier

@Composable
internal expect fun Modifier.desktopScrollRootObserver(
    coordinator: DesktopScrollCoordinator? = null,
): Modifier

@Composable
internal expect fun Modifier.desktopScrollRegionObserver(
    coordinator: DesktopScrollCoordinator? = null,
    owner: DesktopScrollOwner,
    pass: PointerEventPass = PointerEventPass.Main,
): Modifier
