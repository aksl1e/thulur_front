package com.example.thulur

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.example.thulur.di.appModules
import com.example.thulur.presentation.root.AppRootRoute
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).build()
    }

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
