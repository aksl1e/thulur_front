package com.example.thulur.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.thulur.presentation.theme.ThulurTheme

@Composable
fun RootLoadingScreen(
    modifier: Modifier = Modifier,
) {
    val colors = ThulurTheme.SemanticColors.rootLoadingScreen

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenBackground),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = colors.indicatorColor)
    }
}
