package com.example.thulur.data.auth

import com.example.thulur.domain.auth.PasskeyAuthenticationErrorCode
import com.example.thulur.domain.auth.PasskeyAuthenticationException
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur_api.ThulurApi
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.awt.Desktop
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

internal class BrowserPasskeyAuthenticator(
    private val thulurApi: ThulurApi,
    private val browserLauncher: BrowserLauncher = SystemBrowserLauncher,
    private val stateFactory: () -> String = { UUID.randomUUID().toString() },
    private val timeout: Duration = 90.seconds,
) : PasskeyAuthenticator {
    override suspend fun login(email: String): String = authenticate(
        email = email,
        flow = DesktopAuthFlow.Login,
    )

    override suspend fun register(email: String): String = authenticate(
        email = email,
        flow = DesktopAuthFlow.Registration,
    )

    private suspend fun authenticate(
        email: String,
        flow: DesktopAuthFlow,
    ): String {
        val state = stateFactory()
        val address = InetAddress.getByName(CALLBACK_HOST)
        val server = HttpServer.create(InetSocketAddress(address, 0), 0)
        val executor = Executors.newSingleThreadExecutor()
        val callback = CompletableDeferred<DesktopAuthCallback>()

        server.executor = executor
        server.createContext(CALLBACK_PATH) { exchange ->
            handleCallbackExchange(
                exchange = exchange,
                expectedState = state,
                callback = callback,
            )
        }

        return try {
            server.start()
            val callbackUrl = URI(
                "http",
                null,
                CALLBACK_HOST,
                server.address.port,
                CALLBACK_PATH,
                null,
                null,
            ).toString()
            val pageUrl = when (flow) {
                DesktopAuthFlow.Login -> thulurApi.desktopLoginPageUrl(
                    email = email,
                    callbackUrl = callbackUrl,
                    state = state,
                )

                DesktopAuthFlow.Registration -> thulurApi.desktopRegistrationPageUrl(
                    email = email,
                    callbackUrl = callbackUrl,
                    state = state,
                )
            }

            try {
                browserLauncher.open(URI(pageUrl))
            } catch (throwable: Throwable) {
                throw PasskeyAuthenticationException(
                    message = "Could not open browser for passkey sign-in.",
                    code = PasskeyAuthenticationErrorCode.AuthFailed,
                    cause = throwable,
                )
            }

            val result = try {
                withTimeout(timeout) {
                    callback.await()
                }
            } catch (exception: TimeoutCancellationException) {
                throw PasskeyAuthenticationException(
                    message = "Passkey sign-in timed out. Please try again.",
                    code = PasskeyAuthenticationErrorCode.AuthFailed,
                    cause = exception,
                )
            }

            result.error?.let { errorCode ->
                throw PasskeyAuthenticationException(
                    message = errorCode.toUserMessage(),
                    code = errorCode,
                )
            }

            val code = result.code?.takeIf(String::isNotBlank)
                ?: throw PasskeyAuthenticationException(
                    message = "Authentication callback did not include an exchange code.",
                    code = PasskeyAuthenticationErrorCode.MissingCode,
                )

            thulurApi.exchangeAuthCode(
                code = code,
                state = state,
            ).token
        } finally {
            server.stop(0)
            executor.shutdownNow()
        }
    }

    private fun handleCallbackExchange(
        exchange: HttpExchange,
        expectedState: String,
        callback: CompletableDeferred<DesktopAuthCallback>,
    ) {
        try {
            if (exchange.requestMethod != "GET" || exchange.requestURI.path != CALLBACK_PATH) {
                exchange.sendText(
                    statusCode = HTTP_NOT_FOUND,
                    text = "Not found",
                )
                return
            }

            val query = exchange.requestURI.queryParameters()
            val actualState = query["state"]
            if (actualState != expectedState) {
                callback.completeExceptionally(
                    PasskeyAuthenticationException(
                        message = "Authentication callback state did not match.",
                        code = PasskeyAuthenticationErrorCode.InvalidState,
                    ),
                )
                exchange.sendText(
                    statusCode = HTTP_BAD_REQUEST,
                    text = "Authentication state did not match. Return to Thulur and try again.",
                )
                return
            }

            callback.complete(
                DesktopAuthCallback(
                    code = query["code"],
                    error = query["error"],
                ),
            )
            exchange.sendText(
                statusCode = HTTP_OK,
                text = "Authentication finished. You can return to Thulur.",
            )
        } catch (throwable: Throwable) {
            callback.completeExceptionally(
                PasskeyAuthenticationException(
                    message = throwable.message ?: "Authentication callback failed.",
                    code = PasskeyAuthenticationErrorCode.AuthFailed,
                    cause = throwable,
                ),
            )
            exchange.sendText(
                statusCode = HTTP_SERVER_ERROR,
                text = "Authentication callback failed.",
            )
        } finally {
            exchange.close()
        }
    }

    private fun URI.queryParameters(): Map<String, String> {
        val rawQuery = rawQuery ?: return emptyMap()
        return rawQuery
            .split("&")
            .filter(String::isNotBlank)
            .associate { entry ->
                val keyValue = entry.split("=", limit = 2)
                keyValue[0].decodeUrlComponent() to keyValue.getOrElse(1) { "" }.decodeUrlComponent()
            }
    }

    private fun String.decodeUrlComponent(): String =
        URLDecoder.decode(this, StandardCharsets.UTF_8)

    private fun HttpExchange.sendText(
        statusCode: Int,
        text: String,
    ) {
        val bytes = text.encodeToByteArray()
        responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
        sendResponseHeaders(statusCode, bytes.size.toLong())
        responseBody.use { stream ->
            stream.write(bytes)
        }
    }

    private fun String.toUserMessage(): String = when (this) {
        PasskeyAuthenticationErrorCode.UserNotFound -> "No passkeys registered for this email."
        PasskeyAuthenticationErrorCode.PasskeyCancelled -> "Passkey sign-in was cancelled."
        PasskeyAuthenticationErrorCode.PasskeyNotSupported -> "This browser does not support passkeys."
        else -> "Passkey sign-in failed."
    }

    private enum class DesktopAuthFlow {
        Login,
        Registration,
    }

    private data class DesktopAuthCallback(
        val code: String?,
        val error: String?,
    )

    private companion object {
        const val CALLBACK_HOST = "127.0.0.1"
        const val CALLBACK_PATH = "/auth/callback"
        const val HTTP_OK = 200
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_NOT_FOUND = 404
        const val HTTP_SERVER_ERROR = 500
    }
}

internal fun interface BrowserLauncher {
    fun open(uri: URI)
}

internal object SystemBrowserLauncher : BrowserLauncher {
    override fun open(uri: URI) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw IllegalStateException("Desktop browser integration is not supported.")
        }

        Desktop.getDesktop().browse(uri)
    }
}
