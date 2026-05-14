package com.example.thulur.di

import com.example.thulur.data.repository.RemoteThulurApiRepository
import com.example.thulur.data.session.CurrentSessionProviderImpl
import com.example.thulur.data.session.InMemoryReadArticlesCache
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur.domain.auth.providePlatformPasskeyAuthenticator
import com.example.thulur.domain.repository.ThulurApiRepository
import com.example.thulur.domain.session.CurrentSessionProvider
import com.example.thulur.domain.session.ReadArticlesCache
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
import com.example.thulur.domain.usecase.GetDailyFeedUseCase
import com.example.thulur.domain.usecase.GetUserSettingsUseCase
import com.example.thulur.domain.usecase.TerminateAuthSessionUseCase
import com.example.thulur.domain.usecase.UnfollowFeedUseCase
import com.example.thulur.domain.usecase.PatchUserSettingsUseCase
import com.example.thulur.domain.usecase.GetThreadHistoryUseCase
import com.example.thulur.domain.usecase.RateArticleUseCase
import com.example.thulur.domain.usecase.SendGeneralChatMessageUseCase
import com.example.thulur.domain.usecase.SendThreadChatMessageUseCase
import com.example.thulur.presentation.auth.AuthViewModel
import com.example.thulur.presentation.chat.ChatViewModel
import com.example.thulur.presentation.dailyfeed.DailyFeedViewModel
import com.example.thulur.presentation.dailyfeed.article_reader.ArticleReaderViewModel
import com.example.thulur.presentation.dailyfeed.thread_history.ThreadHistoryViewModel
import com.example.thulur.presentation.root.AppRootScreenModel
import com.example.thulur.presentation.settings.SettingsViewModel
import com.example.thulur_api.RemoteThulurApi
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.client.createThulurHttpClient
import com.example.thulur_api.config.ThulurApiConfig
import io.ktor.client.HttpClient
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
    single<ReadArticlesCache> { InMemoryReadArticlesCache() }
    single<CurrentSessionProvider> {
        CurrentSessionProviderImpl(
            tokenStore = get(),
            readArticlesCache = get(),
        )
    }
    single<PasskeyAuthenticator> { providePlatformPasskeyAuthenticator(thulurApi = get()) }
    single<ThulurApiRepository> {
        RemoteThulurApiRepository(
            thulurApi = get(),
            readArticlesCache = get(),
        )
    }
}

val domainModule = module {
    factory { GetDailyFeedUseCase(thulurApiRepository = get()) }
    factory { GetArticleParagraphsUseCase(thulurApiRepository = get()) }
    factory { GetUserSettingsUseCase(thulurApiRepository = get()) }
    factory { PatchUserSettingsUseCase(thulurApiRepository = get()) }
    factory { GetFollowedFeedsUseCase(thulurApiRepository = get()) }
    factory { GetAllFeedsUseCase(thulurApiRepository = get()) }
    factory { FollowFeedUseCase(thulurApiRepository = get()) }
    factory { UnfollowFeedUseCase(thulurApiRepository = get()) }
    factory { GetCurrentUserUseCase(thulurApiRepository = get()) }
    factory { GetAuthSessionsUseCase(thulurApiRepository = get()) }
    factory { TerminateAuthSessionUseCase(thulurApiRepository = get()) }
    factory { GetThreadHistoryUseCase(thulurApiRepository = get()) }
    factory { RateArticleUseCase(thulurApiRepository = get()) }
    factory { SendGeneralChatMessageUseCase(thulurApiRepository = get()) }
    factory { SendThreadChatMessageUseCase(thulurApiRepository = get()) }
}

val presentationModule = module {
    factory { AppRootScreenModel(get(), get(), get(), get()) }
    factory { AuthViewModel(get(), get()) }
    factory { DailyFeedViewModel(get(), get()) }
    factory { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { (title: String, mode: com.example.thulur.presentation.router.ChatMode) ->
        ChatViewModel(title = title, mode = mode, sendGeneralChatMessageUseCase = get(), sendThreadChatMessageUseCase = get())
    }
    factory { (threadId: String, threadName: String, initialDay: kotlinx.datetime.LocalDate) ->
        ThreadHistoryViewModel(
            threadId = threadId,
            threadName = threadName,
            initialDay = initialDay,
            getThreadHistoryUseCase = get(),
            readArticlesCache = get(),
        )
    }
    factory { (articleId: String, title: String, url: String, isRead: Boolean) ->
        ArticleReaderViewModel(
            articleId = articleId,
            title = title,
            url = url,
            isRead = isRead,
            getArticleParagraphsUseCase = get(),
            rateArticleUseCase = get(),
            readArticlesCache = get(),
        )
    }
}

val appModules = listOf(
    apiModule,
    dataModule,
    domainModule,
    presentationModule,
)
