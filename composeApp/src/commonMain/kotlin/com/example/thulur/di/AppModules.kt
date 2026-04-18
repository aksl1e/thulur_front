package com.example.thulur.di

import com.example.thulur.data.repository.RemoteThulurApiRepository
import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur.domain.auth.providePlatformPasskeyAuthenticator
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.session.SecureTokenStore
import com.example.thulur.domain.session.providePlatformSecureTokenStore
import com.example.thulur.domain.theme.ThemeStore
import com.example.thulur.domain.theme.providePlatformThemeStore
import com.example.thulur.domain.usecase.FollowFeedUseCase
import com.example.thulur.domain.usecase.GetAllFeedsUseCase
import com.example.thulur.domain.usecase.GetArticleParagraphsUseCase
import com.example.thulur.domain.usecase.GetAuthSessionsUseCase
import com.example.thulur.domain.usecase.GetCurrentUserUseCase
import com.example.thulur.domain.usecase.GetFollowedFeedsUseCase
import com.example.thulur.domain.usecase.GetMainFeedUseCase
<<<<<<< HEAD
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.domain.usecase.TerminateAuthSessionUseCase
import com.example.thulur.domain.usecase.UnfollowFeedUseCase
import com.example.thulur.domain.usecase.PatchUserSettingsUseCase
=======
import com.example.thulur.domain.usecase.GetThreadHistoryUseCase
>>>>>>> 7ec637d (Add thread history API and shared article models)
import com.example.thulur.presentation.auth.AuthViewModel
import com.example.thulur.presentation.mainfeed.MainFeedViewModel
import com.example.thulur.presentation.root.AppRootViewModel
import com.example.thulur.presentation.settings.SettingsViewModel
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
    single<ThemeStore> { providePlatformThemeStore() }
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
<<<<<<< HEAD
    factory { GetUserSettingsUseCase(thulurApiRepository = get()) }
    factory { PatchUserSettingsUseCase(thulurApiRepository = get()) }
    factory { GetFollowedFeedsUseCase(thulurApiRepository = get()) }
    factory { GetAllFeedsUseCase(thulurApiRepository = get()) }
    factory { FollowFeedUseCase(thulurApiRepository = get()) }
    factory { UnfollowFeedUseCase(thulurApiRepository = get()) }
    factory { GetCurrentUserUseCase(thulurApiRepository = get()) }
    factory { GetAuthSessionsUseCase(thulurApiRepository = get()) }
    factory { TerminateAuthSessionUseCase(thulurApiRepository = get()) }
=======
    factory { GetThreadHistoryUseCase(thulurApiRepository = get()) }
>>>>>>> 7ec637d (Add thread history API and shared article models)
}

val presentationModule = module {
    viewModelOf(::AppRootViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::MainFeedViewModel)
    viewModelOf(::SettingsViewModel)
}

val appModules = listOf(
    apiModule,
    dataModule,
    domainModule,
    presentationModule,
)
