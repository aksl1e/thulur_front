package com.example.thulur.di

import com.example.thulur.data.repository.RemoteThulurApiRepository
import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur.domain.auth.providePlatformPasskeyAuthenticator
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.session.SecureTokenStore
import com.example.thulur.domain.session.providePlatformSecureTokenStore
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.GetMainFeedUseCase
import com.example.thulur.presentation.auth.AuthViewModel
import com.example.thulur.presentation.mainfeed.MainFeedViewModel
import com.example.thulur.presentation.root.AppRootViewModel
import com.example.thulur_api.RemoteThulurApi
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.client.createThulurHttpClient
import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val apiModule = module {
    single { ThulurApiConfig() }
    single<HttpClient> {
        val sessionProvider = get<CurrentSessionProvider>()
        createThulurHttpClient(
            currentTokenProvider = sessionProvider::currentToken,
            onUnauthorized = sessionProvider::clearToken,
        )
    }
    single<ThulurApi> { RemoteThulurApi(httpClient = get(), config = get()) }
}

val dataModule = module {
    single<SecureTokenStore> { providePlatformSecureTokenStore() }
    single<CurrentSessionProvider> { CurrentSessionProviderImpl(tokenStore = get()) }
    single<PasskeyAuthenticator> { providePlatformPasskeyAuthenticator(thulurApi = get()) }
    single<ThulurApiRepository> {
        RemoteThulurApiRepository(
            thulurApi = get(),
        )
    }
}

val domainModule = module {
    factory { GetMainFeedUseCase(thulurApiRepository = get()) }
    factory { GetArticleParagraphsUseCase(thulurApiRepository = get()) }
}

val presentationModule = module {
    viewModelOf(::AppRootViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::MainFeedViewModel)
}

val appModules = listOf(
    apiModule,
    dataModule,
    domainModule,
    presentationModule,
)
