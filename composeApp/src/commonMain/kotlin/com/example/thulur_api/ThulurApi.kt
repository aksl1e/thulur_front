package com.example.thulur_api

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.auth.AuthTokenDto
import com.example.thulur_api.methods.auth.DesktopAuthExchangeMethod
import com.example.thulur_api.methods.auth.DesktopLoginPageUrlMethod
import com.example.thulur_api.methods.auth.DesktopRegistrationPageUrlMethod
import com.example.thulur_api.methods.daily_feed.DailyFeedMethod
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

/**
 * Public transport boundary for all backend methods used by the app.
 *
 * The implementation is intentionally kept behind a single facade so the app
 * depends on one API boundary instead of wiring individual HTTP methods into
 * feature layers.
 */
interface ThulurApi {
    /**
     * Returns the raw daily feed payload for the bearer-authenticated user.
     *
     * @param day Optional day filter. When `null`, the backend resolves it to the current day.
     * @return Raw thread/article DTOs exactly as needed by the app data layer.
     */
    suspend fun getDailyFeed(
        day: LocalDate? = null,
    ): List<DailyFeedThreadDto>

    fun desktopRegistrationPageUrl(
        email: String,
        callbackUrl: String,
        state: String,
    ): String

    fun desktopLoginPageUrl(
        email: String,
        callbackUrl: String,
        state: String,
    ): String

    suspend fun exchangeAuthCode(
        code: String,
        state: String,
    ): AuthTokenDto
}

/**
 * Remote Ktor-backed implementation of [ThulurApi].
 */
class RemoteThulurApi(
    httpClient: HttpClient,
    config: ThulurApiConfig,
) : ThulurApi {
    private val desktopRegistrationPageUrlMethod = DesktopRegistrationPageUrlMethod(
        config = config,
    )
    private val desktopLoginPageUrlMethod = DesktopLoginPageUrlMethod(
        config = config,
    )
    private val desktopAuthExchangeMethod = DesktopAuthExchangeMethod(
        httpClient = httpClient,
        config = config,
    )
    private val dailyFeedMethod = DailyFeedMethod(
        httpClient = httpClient,
        config = config,
    )

    override suspend fun getDailyFeed(
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = dailyFeedMethod.execute(
        day = day,
    )

    override fun desktopRegistrationPageUrl(
        email: String,
        callbackUrl: String,
        state: String,
    ): String = desktopRegistrationPageUrlMethod.execute(
        email = email,
        callbackUrl = callbackUrl,
        state = state,
    )

    override fun desktopLoginPageUrl(
        email: String,
        callbackUrl: String,
        state: String,
    ): String = desktopLoginPageUrlMethod.execute(
        email = email,
        callbackUrl = callbackUrl,
        state = state,
    )

    override suspend fun exchangeAuthCode(
        code: String,
        state: String,
    ): AuthTokenDto = desktopAuthExchangeMethod.execute(
        code = code,
        state = state,
    )
}
