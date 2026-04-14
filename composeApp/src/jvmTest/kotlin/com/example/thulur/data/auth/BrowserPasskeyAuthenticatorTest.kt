package com.example.thulur.data.auth

import com.example.thulur.domain.auth.PasskeyAuthenticationErrorCode
import com.example.thulur.domain.auth.PasskeyAuthenticationException
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.DailyFeedThreadDto
import com.example.thulur_api.dtos.auth.AuthTokenDto
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

        launcher.awaitUri()
        httpGet("${api.loginCallbackUrl}?code=code-1&state=state-1")

        assertEquals("login-token", login.await())
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

        launcher.awaitUri()
        httpGet("${api.registrationCallbackUrl}?code=register-code&state=state-1")

        assertEquals("register-token", registration.await())
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

    override fun desktopRegistrationPageUrl(
        email: String,
        callbackUrl: String,
        state: String,
    ): String {
        registrationCallbackUrl = callbackUrl
        return "http://localhost:8002/auth/register?email=${email.urlEncode()}&state=${state.urlEncode()}"
    }

    override fun desktopLoginPageUrl(
        email: String,
        callbackUrl: String,
        state: String,
    ): String {
        loginCallbackUrl = callbackUrl
        return "http://localhost:8002/auth/login?email=${email.urlEncode()}&state=${state.urlEncode()}"
    }

    override suspend fun exchangeAuthCode(
        code: String,
        state: String,
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

private fun String.urlEncode(): String =
    URLEncoder.encode(this, StandardCharsets.UTF_8)
