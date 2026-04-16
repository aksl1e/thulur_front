package com.example.thulur_api

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.auth.AuthTokenDto
import com.example.thulur_api.dtos.auth.DesktopAuthMode
import com.example.thulur_api.dtos.auth.DesktopAuthStartDto
import com.example.thulur_api.methods.auth.DesktopAuthExchangeMethod
import com.example.thulur_api.methods.auth.DesktopAuthStartMethod
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

    suspend fun startDesktopAuth(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto

    suspend fun exchangeAuthCode(
        code: String,
        state: String,
        deviceName: String? = null,
        platform: String? = null,
    ): AuthTokenDto
}

/**
 * Remote Ktor-backed implementation of [ThulurApi].
 */
class RemoteThulurApi(
    httpClient: HttpClient,
    config: ThulurApiConfig,
) : ThulurApi {
    private val desktopAuthStartMethod = DesktopAuthStartMethod(
        httpClient = httpClient,
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

    override suspend fun startDesktopAuth(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto = desktopAuthStartMethod.execute(
        email = email,
        mode = mode,
        callbackUrl = callbackUrl,
        state = state,
    )

    override suspend fun exchangeAuthCode(
        code: String,
        state: String,
        deviceName: String?,
        platform: String?,
    ): AuthTokenDto = desktopAuthExchangeMethod.execute(
        code = code,
        state = state,
        deviceName = deviceName,
        platform = platform,
    )
}
