package com.example.thulur_api

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.auth.AuthStatusDto
import com.example.thulur_api.dtos.auth.AuthenticationOptionsDto
import com.example.thulur_api.dtos.auth.RegistrationOptionsDto
import com.example.thulur_api.methods.auth.LoginBeginMethod
import com.example.thulur_api.methods.auth.LoginFinishMethod
import com.example.thulur_api.methods.auth.RegisterBeginMethod
import com.example.thulur_api.methods.auth.RegisterFinishMethod
import com.example.thulur_api.methods.daily_feed.DailyFeedMethod
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

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

    /**
     * Requests WebAuthn registration options for the given email.
     */
    suspend fun beginRegistration(email: String): RegistrationOptionsDto

    /**
     * Finishes WebAuthn registration with the credential returned by the platform.
     */
    suspend fun finishRegistration(
        email: String,
        credential: JsonObject,
    ): AuthStatusDto

    /**
     * Requests WebAuthn authentication options for the given email.
     */
    suspend fun beginLogin(email: String): AuthenticationOptionsDto

    /**
     * Finishes WebAuthn login with the credential returned by the platform.
     */
    suspend fun finishLogin(
        email: String,
        credential: JsonObject,
    ): AuthStatusDto
}

/**
 * Remote Ktor-backed implementation of [ThulurApi].
 */
class RemoteThulurApi(
    httpClient: HttpClient,
    config: ThulurApiConfig,
) : ThulurApi {
    private val registerBeginMethod = RegisterBeginMethod(
        httpClient = httpClient,
        config = config,
    )
    private val registerFinishMethod = RegisterFinishMethod(
        httpClient = httpClient,
        config = config,
    )
    private val loginBeginMethod = LoginBeginMethod(
        httpClient = httpClient,
        config = config,
    )
    private val loginFinishMethod = LoginFinishMethod(
        httpClient = httpClient,
        config = config,
    )
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

    override suspend fun beginRegistration(email: String): RegistrationOptionsDto =
        registerBeginMethod.execute(email = email)

    override suspend fun finishRegistration(
        email: String,
        credential: JsonObject,
    ): AuthStatusDto = registerFinishMethod.execute(
        email = email,
        credential = credential,
    )

    override suspend fun beginLogin(email: String): AuthenticationOptionsDto =
        loginBeginMethod.execute(email = email)

    override suspend fun finishLogin(
        email: String,
        credential: JsonObject,
    ): AuthStatusDto = loginFinishMethod.execute(
        email = email,
        credential = credential,
    )
}
