package com.example.thulur

import androidx.compose.runtime.Composable
import com.example.thulur.di.appModules
import com.example.thulur.presentation.mainfeed.MainFeedRoute
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun App(mode: ThemeMode = ThemeMode.Light) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appModules)
        },
    ) {
        ThulurTheme(mode = mode) {
            MainFeedRoute()
        }
    }
}
