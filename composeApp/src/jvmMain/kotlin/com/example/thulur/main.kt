package com.example.thulur

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.thulur.presentation.dailyfeed.article_reader.JcefBrowserRuntime

fun main() = application {
    JcefBrowserRuntime.prewarm()

    Window(
        onCloseRequest = {
            JcefBrowserRuntime.shutdown()
            exitApplication()
        },
        title = "",
    ) {
        App()
    }
}
