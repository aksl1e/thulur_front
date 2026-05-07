package com.example.thulur.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
@ReadOnlyComposable
fun thulurDefaultCornerRadius(): Dp = 3.thulurDp()

@Composable
@ReadOnlyComposable
fun thulurDefaultChatCornerRadius(): Dp = 40.thulurDp()

@Composable
@ReadOnlyComposable
fun thulurDefaultShape(): Shape = RoundedCornerShape(thulurDefaultCornerRadius())
