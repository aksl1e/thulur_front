package com.example.thulur

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.thulur.presentation.theme.ThemeMode

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "thulur",
    ) {
        App(mode = ThemeMode.Dark)
    }
}
