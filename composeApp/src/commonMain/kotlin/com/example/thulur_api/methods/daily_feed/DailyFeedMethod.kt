package com.example.thulur_api.methods.daily_feed

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.DailyFeedDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.datetime.LocalDate

/**
 * Encapsulates the `/users/me/daily_feed` transport call.
 */
internal class DailyFeedMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    /**
     * Requests the daily feed for the bearer-authenticated user.
     *
     * @param day Optional day filter in `YYYY-MM-DD` format. When `null`,
     * the backend uses its default "today" behavior.
     * @return Raw backend response as a [DailyFeedDto].
     */
    suspend fun execute(
        day: LocalDate? = null,
    ): DailyFeedDto = httpClient
        .get {
            url("${config.baseUrl}/users/me/daily_feed")
            day?.let { parameter("day", it.toString()) }
        }
        .body()
}
