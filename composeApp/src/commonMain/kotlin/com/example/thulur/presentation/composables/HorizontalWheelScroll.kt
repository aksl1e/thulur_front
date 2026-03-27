package com.example.thulur.presentation.composables

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.desktopHorizontalWheelScroll(
    state: LazyListState,
): Modifier
