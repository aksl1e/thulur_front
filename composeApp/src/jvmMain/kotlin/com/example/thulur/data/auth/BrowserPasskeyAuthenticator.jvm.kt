package com.example.thulur.data.auth

import com.example.thulur.domain.auth.PasskeyAuthenticationErrorCode
import com.example.thulur.domain.auth.PasskeyAuthenticationException
import com.example.thulur.domain.auth.PasskeyAuthenticator
import com.example.thulur_api.ThulurApi
import com.example.thulur_api.dtos.auth.DesktopAuthMode
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
            val browserUrl = thulurApi.startDesktopAuth(
                email = email,
                mode = when (flow) {
                    DesktopAuthFlow.Login -> DesktopAuthMode.Login
                    DesktopAuthFlow.Registration -> DesktopAuthMode.Register
                },
                callbackUrl = callbackUrl,
                state = state,
            ).browserUrl

            try {
                browserLauncher.open(URI(browserUrl))
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
                exchange.sendHtml(
                    statusCode = HTTP_BAD_REQUEST,
                    html = CALLBACK_ERROR_HTML,
                )
                return
            }

            callback.complete(
                DesktopAuthCallback(
                    code = query["code"],
                    error = query["error"],
                ),
            )
            exchange.sendHtml(
                statusCode = HTTP_OK,
                html = CALLBACK_SUCCESS_HTML,
            )
        } catch (throwable: Throwable) {
            callback.completeExceptionally(
                PasskeyAuthenticationException(
                    message = throwable.message ?: "Authentication callback failed.",
                    code = PasskeyAuthenticationErrorCode.AuthFailed,
                    cause = throwable,
                ),
            )
            exchange.sendHtml(
                statusCode = HTTP_SERVER_ERROR,
                html = CALLBACK_ERROR_HTML,
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

    private fun HttpExchange.sendHtml(
        statusCode: Int,
        html: String,
    ) {
        val bytes = html.encodeToByteArray()
        responseHeaders.add("Content-Type", "text/html; charset=utf-8")
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
        val CALLBACK_SUCCESS_HTML = """
        <!DOCTYPE html>
        <html><head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>Thulur</title>
        <style>
          @import url('https://fonts.googleapis.com/css2?family=Lora:wght@600&family=Public+Sans:wght@400;500&display=swap');
          :root { --primary: #3B82F6; --slate: #64748B; --success: #22C55E; }
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { font-family: 'Public Sans', system-ui, sans-serif; background: #F8FAFC;
                 min-height: 100vh; display: flex; align-items: center; justify-content: center; }
          .card { background: #fff; border-radius: 16px; padding: 48px 40px; text-align: center;
                  width: 100%; max-width: 400px;
                  box-shadow: 0 1px 3px rgba(0,0,0,.06), 0 4px 16px rgba(0,0,0,.08); }
          h2 { font-family: 'Lora', serif; font-weight: 600; font-size: 22px;
               color: #0F172A; margin-bottom: 12px; }
          p { font-size: 14px; font-weight: 500; color: var(--slate); }
        </style>
        </head><body>
        <div class="card">
          <h2>You're signed in.</h2>
          <p>You can close this tab and return to Thulur.</p>
        </div>
        </body></html>
    """.trimIndent()
        val CALLBACK_ERROR_HTML = """
        <!DOCTYPE html>
        <html><head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>Thulur</title>
        <style>
          @import url('https://fonts.googleapis.com/css2?family=Lora:wght@600&family=Public+Sans:wght@400;500&display=swap');
          :root { --error: #EF4444; --slate: #64748B; }
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { font-family: 'Public Sans', system-ui, sans-serif; background: #F8FAFC;
                 min-height: 100vh; display: flex; align-items: center; justify-content: center; }
          .card { background: #fff; border-radius: 16px; padding: 48px 40px; text-align: center;
                  width: 100%; max-width: 400px;
                  box-shadow: 0 1px 3px rgba(0,0,0,.06), 0 4px 16px rgba(0,0,0,.08); }
          h2 { font-family: 'Lora', serif; font-weight: 600; font-size: 22px;
               color: var(--error); margin-bottom: 12px; }
          p { font-size: 14px; font-weight: 500; color: var(--slate); }
        </style>
        </head><body>
        <div class="card">
          <h2>Something went wrong.</h2>
          <p>Return to Thulur and try again.</p>
        </div>
        </body></html>
    """.trimIndent()
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
