package com.example.thulur.di

import com.example.thulur.data.repository.RemoteThulurApiRepository
import com.example.thulur.data.session.DevCurrentUserProvider
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.session.CurrentUserProvider
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import com.example.thulur.presentation.mainfeed.MainFeedViewModel
import com.example.thulur_api.RemoteThulurApi
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.client.createThulurHttpClient
import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val apiModule = module {
    single { ThulurApiConfig() }
    single<HttpClient> { createThulurHttpClient() }
    single<ThulurApi> { RemoteThulurApi(httpClient = get(), config = get()) }
}

val dataModule = module {
    single<CurrentUserProvider> { DevCurrentUserProvider() }
    single<ThulurApiRepository> {
        RemoteThulurApiRepository(
            thulurApi = get(),
            currentUserProvider = get(),
        )
    }
}

val domainModule = module {
    factory { GetMainFeedUseCase(thulurApiRepository = get()) }
}

val presentationModule = module {
    viewModelOf(::MainFeedViewModel)
}

val appModules = listOf(
    apiModule,
    dataModule,
    domainModule,
    presentationModule,
)
