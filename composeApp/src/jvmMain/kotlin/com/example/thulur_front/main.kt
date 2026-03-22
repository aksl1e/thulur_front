package com.example.thulur_front

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.thulur_front.theme.ThemeMode

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "thulur_front",
    ) {
        App(mode = ThemeMode.Dark)
    }
}