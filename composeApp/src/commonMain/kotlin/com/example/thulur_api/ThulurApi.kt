package com.example.thulur_api

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.DailyFeedThreadDto
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
     * Returns the raw daily feed payload for the given user.
     *
     * @param userId Backend user identifier used in the endpoint path.
     * @param day Optional day filter. When `null`, the backend resolves it to the current day.
     * @return Raw thread/article DTOs exactly as needed by the app data layer.
     */
    suspend fun getDailyFeed(
        userId: String,
        day: LocalDate? = null,
    ): List<DailyFeedThreadDto>
}

/**
 * Remote Ktor-backed implementation of [ThulurApi].
 */
class RemoteThulurApi(
    httpClient: HttpClient,
    config: ThulurApiConfig,
) : ThulurApi {
    private val dailyFeedMethod = DailyFeedMethod(
        httpClient = httpClient,
        config = config,
    )

    override suspend fun getDailyFeed(
        userId: String,
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = dailyFeedMethod.execute(
        userId = userId,
        day = day,
    )
}
