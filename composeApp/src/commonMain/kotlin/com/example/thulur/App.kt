package com.example.thulur

import androidx.compose.runtime.Composable
import com.example.thulur.di.appModules
import com.example.thulur.presentation.root.AppRootRoute
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appModules)
        },
    ) {
        ProvideThulurDesignScale {
            AppRootRoute()
        }
    }
}
