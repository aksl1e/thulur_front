package com.example.thulur.data.auth

import com.example.thulur.domain.auth.PasskeyAuthenticationErrorCode
import com.example.thulur.domain.auth.PasskeyAuthenticationException
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.AuthSessionDto
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.FeedDto
import com.example.thulur_api.dtos.ParagraphDto
import com.example.thulur_api.dtos.UpdateUserSettingsDto
import com.example.thulur_api.dtos.UserDto
import com.example.thulur_api.dtos.UserSettingsDto
import com.example.thulur_api.dtos.auth.AuthTokenDto
import com.example.thulur_api.dtos.auth.DesktopAuthMode
import com.example.thulur_api.dtos.auth.DesktopAuthStartDto
import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate

class BrowserPasskeyAuthenticatorTest {
    @Test
    fun `login success callback exchanges code and returns token`() = runTest {
        val api = FakeDesktopAuthApi(exchangeToken = "login-token")
        val launcher = CapturingBrowserLauncher()
        val authenticator = BrowserPasskeyAuthenticator(
            thulurApi = api,
            browserLauncher = launcher,
            stateFactory = { "state-1" },
            timeout = 5.seconds,
        )

        val login = async {
            authenticator.login("hello@example.com")
        }

        val launchedUri = launcher.awaitUri()
        assertEquals("http://localhost:8002/auth/flow/login-flow", launchedUri.toString())
        assertFalse(launchedUri.toString().contains("hello@example.com"))
        httpGet("${api.loginCallbackUrl}?code=code-1&state=state-1")

        assertEquals("login-token", login.await())
        assertEquals(DesktopAuthMode.Login, api.startedMode)
        assertEquals("hello@example.com", api.startedEmail)
        assertEquals("code-1", api.exchangedCode)
        assertEquals("state-1", api.exchangedState)
    }

    @Test
    fun `wrong callback state fails and does not exchange code`() = runTest {
        val api = FakeDesktopAuthApi(exchangeToken = "login-token")
        val launcher = CapturingBrowserLauncher()
        val authenticator = BrowserPasskeyAuthenticator(
            thulurApi = api,
            browserLauncher = launcher,
            stateFactory = { "state-1" },
            timeout = 5.seconds,
        )

        supervisorScope {
            val login = async {
                authenticator.login("hello@example.com")
            }

            launcher.awaitUri()
            httpGet("${api.loginCallbackUrl}?code=code-1&state=wrong-state", expectedStatus = 400)

            val exception = assertFailsWith<PasskeyAuthenticationException> {
                login.await()
            }
            assertEquals(PasskeyAuthenticationErrorCode.InvalidState, exception.code)
            assertEquals(null, api.exchangedCode)
        }
    }

    @Test
    fun `user not found callback is surfaced as typed auth error`() = runTest {
        val api = FakeDesktopAuthApi(exchangeToken = "login-token")
        val launcher = CapturingBrowserLauncher()
        val authenticator = BrowserPasskeyAuthenticator(
            thulurApi = api,
            browserLauncher = launcher,
            stateFactory = { "state-1" },
            timeout = 5.seconds,
        )

        supervisorScope {
            val login = async {
                authenticator.login("hello@example.com")
            }

            launcher.awaitUri()
            httpGet("${api.loginCallbackUrl}?error=USER_NOT_FOUND&state=state-1")

            val exception = assertFailsWith<PasskeyAuthenticationException> {
                login.await()
            }
            assertEquals(PasskeyAuthenticationErrorCode.UserNotFound, exception.code)
            assertEquals(null, api.exchangedCode)
        }
    }

    @Test
    fun `registration success callback exchanges code and returns token`() = runTest {
        val api = FakeDesktopAuthApi(exchangeToken = "register-token")
        val launcher = CapturingBrowserLauncher()
        val authenticator = BrowserPasskeyAuthenticator(
            thulurApi = api,
            browserLauncher = launcher,
            stateFactory = { "state-1" },
            timeout = 5.seconds,
        )

        val registration = async {
            authenticator.register("hello@example.com")
        }

        val launchedUri = launcher.awaitUri()
        assertEquals("http://localhost:8002/auth/flow/register-flow", launchedUri.toString())
        assertFalse(launchedUri.toString().contains("hello@example.com"))
        httpGet("${api.registrationCallbackUrl}?code=register-code&state=state-1")

        assertEquals("register-token", registration.await())
        assertEquals(DesktopAuthMode.Register, api.startedMode)
        assertEquals("hello@example.com", api.startedEmail)
        assertEquals("register-code", api.exchangedCode)
        assertEquals("state-1", api.exchangedState)
    }
}

private class CapturingBrowserLauncher : BrowserLauncher {
    private val uri = CompletableDeferred<URI>()

    override fun open(uri: URI) {
        this.uri.complete(uri)
    }

    suspend fun awaitUri(): URI = withTimeout(5.seconds) {
        uri.await()
    }
}

private class FakeDesktopAuthApi(
    private val exchangeToken: String,
) : ThulurApi {
    var startedEmail: String? = null
        private set
    var startedMode: DesktopAuthMode? = null
        private set
    lateinit var loginCallbackUrl: String
        private set
    lateinit var registrationCallbackUrl: String
        private set
    var exchangedCode: String? = null
        private set
    var exchangedState: String? = null
        private set

    override suspend fun getDailyFeed(
        day: LocalDate?,
    ): List<DailyFeedThreadDto> = error("Not used in this test")

    override suspend fun getArticleParagraphs(
        articleId: String,
    ): List<ParagraphDto> = error("Not used in this test")

    override suspend fun getUserSettings(): UserSettingsDto = error("Not used in this test")

    override suspend fun patchUserSettings(patch: UpdateUserSettingsDto): UserSettingsDto =
        error("Not used in this test")

    override suspend fun getFollowedFeeds(): List<FeedDto> = error("Not used in this test")

    override suspend fun getAllFeeds(): List<FeedDto> = error("Not used in this test")

    override suspend fun followFeed(feedId: String) = error("Not used in this test")

    override suspend fun unfollowFeed(feedId: String) = error("Not used in this test")

    override suspend fun getCurrentUser(): UserDto = error("Not used in this test")

    override suspend fun getAuthSessions(): List<AuthSessionDto> = error("Not used in this test")

    override suspend fun terminateAuthSession(sessionId: String) = error("Not used in this test")

    override suspend fun startDesktopAuth(
        email: String,
        mode: DesktopAuthMode,
        callbackUrl: String,
        state: String,
    ): DesktopAuthStartDto {
        startedEmail = email
        startedMode = mode
        when (mode) {
            DesktopAuthMode.Login -> loginCallbackUrl = callbackUrl
            DesktopAuthMode.Register -> registrationCallbackUrl = callbackUrl
        }

        return DesktopAuthStartDto(
            browserUrl = when (mode) {
                DesktopAuthMode.Login -> "http://localhost:8002/auth/flow/login-flow"
                DesktopAuthMode.Register -> "http://localhost:8002/auth/flow/register-flow"
            },
        )
    }

    override suspend fun exchangeAuthCode(
        code: String,
        state: String,
        deviceName: String?,
        platform: String?,
    ): AuthTokenDto {
        exchangedCode = code
        exchangedState = state
        return AuthTokenDto(token = exchangeToken)
    }
}

private fun httpGet(
    url: String,
    expectedStatus: Int = 200,
): String {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    assertEquals(expectedStatus, connection.responseCode)
    val stream = if (connection.responseCode < 400) {
        connection.inputStream
    } else {
        connection.errorStream
    }
    return stream.bufferedReader().use { it.readText() }
}
