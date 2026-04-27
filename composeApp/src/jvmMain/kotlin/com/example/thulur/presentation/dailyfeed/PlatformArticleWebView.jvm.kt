package com.example.thulur.presentation.dailyfeed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.example.thulur.domain.model.ArticleParagraph
import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlinx.serialization.json.Json
import org.cef.browser.CefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLifeSpanHandlerAdapter
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter

private val articleReaderBrowserJson = Json {
    ignoreUnknownKeys = true
}

@Composable
internal actual fun PlatformArticleWebView(
    initialUrl: String,
    paragraphs: List<ArticleParagraph>,
    areParagraphsReady: Boolean,
    isArticleRead: Boolean,
    modifier: Modifier,
    onInitialPageLoaded: () -> Unit,
    onInjectionSucceeded: () -> Unit,
    onProgressChanged: (Float) -> Unit,
    onRateArticle: (Int) -> Unit,
    onError: (String) -> Unit,
) {
    val controller = remember(initialUrl) {
        JcefArticleWebViewController(initialUrl = initialUrl)
    }

    DisposableEffect(controller) {
        onDispose {
            controller.dispose()
        }
    }

    SwingPanel(
        modifier = modifier,
        factory = { controller.panel },
        update = {
            controller.update(
                paragraphs = paragraphs,
                areParagraphsReady = areParagraphsReady,
                isArticleRead = isArticleRead,
                onInitialPageLoaded = onInitialPageLoaded,
                onInjectionSucceeded = onInjectionSucceeded,
                onProgressChanged = onProgressChanged,
                onRateArticle = onRateArticle,
                onError = onError,
            )
        },
    )
}

internal fun parseArticleReaderBridgeMessage(
    raw: String?,
): ArticleReaderBridgeMessage? = raw
    ?.takeIf { it.isNotBlank() }
    ?.let { request ->
        runCatching {
            articleReaderBrowserJson.decodeFromString<ArticleReaderBridgeMessage>(request)
        }.getOrNull()
    }

private class JcefArticleWebViewController(
    private val initialUrl: String,
) {
    private val disposed = AtomicBoolean(false)
    private val bridgeHandler = ArticleReaderBridgeHandler()

    private var client: org.cef.CefClient? = null
    private var browser: CefBrowser? = null
    private var messageRouter: CefMessageRouter? = null

    private var paragraphs: List<ArticleParagraph> = emptyList()
    private var areParagraphsReady: Boolean = false
    private var isArticleRead: Boolean = false
    private var onInitialPageLoaded: () -> Unit = {}
    private var onInjectionSucceeded: () -> Unit = {}
    private var onProgressChanged: (Float) -> Unit = {}
    private var onRateArticle: (Int) -> Unit = {}
    private var onError: (String) -> Unit = {}
    private var resolvedInitialUrl: String? = null
    private var initialPageLoaded = false
    private var injectionRequested = false
    private val pendingErrors = mutableListOf<String>()

    val panel = JPanel(BorderLayout()).apply {
        isOpaque = true
        isVisible = true
    }

    init {
        createBrowserAsync()
    }

    fun update(
        paragraphs: List<ArticleParagraph>,
        areParagraphsReady: Boolean,
        isArticleRead: Boolean,
        onInitialPageLoaded: () -> Unit,
        onInjectionSucceeded: () -> Unit,
        onProgressChanged: (Float) -> Unit,
        onRateArticle: (Int) -> Unit,
        onError: (String) -> Unit,
    ) {
        this.paragraphs = paragraphs
        this.areParagraphsReady = areParagraphsReady
        this.isArticleRead = isArticleRead
        this.onInitialPageLoaded = onInitialPageLoaded
        this.onInjectionSucceeded = onInjectionSucceeded
        this.onProgressChanged = onProgressChanged
        this.onRateArticle = onRateArticle
        this.onError = onError

        flushPendingErrors()
        maybeInjectScript()
    }

    fun dispose() {
        if (!disposed.compareAndSet(false, true)) return

        SwingUtilities.invokeLater {
            messageRouter?.let { router ->
                client?.removeMessageRouter(router)
                router.dispose()
            }
            messageRouter = null

            browser?.stopLoad()
            browser?.close(true)
            browser = null

            client?.dispose()
            client = null

            panel.removeAll()
            panel.revalidate()
            panel.repaint()
        }
    }

    private fun createBrowserAsync() {
        articleReaderDebugLog("createBrowserAsync: starting, url=$initialUrl")
        JcefBrowserRuntime.createClientAsync().thenAccept { result ->
            articleReaderDebugLog("createBrowserAsync: createClientAsync completed, isSuccess=${result.isSuccess}")
            result.fold(
                onSuccess = { createdClient ->
                    SwingUtilities.invokeLater {
                        articleReaderDebugLog("createBrowserAsync: onSuccess on EDT, disposed=${disposed.get()}")
                        if (disposed.get()) {
                            createdClient.dispose()
                            return@invokeLater
                        }

                        client = createdClient
                        configureClient(createdClient)

                        articleReaderDebugLog("createBrowserAsync: calling createBrowser url=$initialUrl")
                        val createdBrowser = createdClient.createBrowser(initialUrl, false, false)
                        browser = createdBrowser
                        panel.add(createdBrowser.uiComponent, BorderLayout.CENTER)
                        createdBrowser.uiComponent.isVisible = true
                        createdBrowser.createImmediately()
                        panel.revalidate()
                        panel.repaint()
                        articleReaderDebugLog("createBrowserAsync: browser created and panel updated")
                    }
                },
                onFailure = { throwable ->
                    articleReaderDebugLog("createBrowserAsync: FAILURE ${throwable::class.simpleName}: ${throwable.message}")
                    reportError(throwable.message ?: "Failed to initialize article reader.")
                },
            )
        }
    }

    private fun configureClient(client: org.cef.CefClient) {
        messageRouter = CefMessageRouter.create().also { router ->
            router.addHandler(bridgeHandler, true)
            client.addMessageRouter(router)
        }

        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser, frame: org.cef.browser.CefFrame, httpStatusCode: Int) {
                if (disposed.get() || !frame.isMain()) return

                articleReaderDebugLog(
                    "onLoadEnd url=${browser.url} httpStatusCode=$httpStatusCode initialResolved=$resolvedInitialUrl",
                )

                if (resolvedInitialUrl == null) {
                    resolvedInitialUrl = browser.url
                }
                initialPageLoaded = true
                injectionRequested = false
                onInitialPageLoaded()
                maybeInjectScript()
            }

            override fun onLoadError(
                browser: CefBrowser,
                frame: org.cef.browser.CefFrame,
                errorCode: CefLoadHandler.ErrorCode,
                errorText: String,
                failedUrl: String,
            ) {
                if (disposed.get() || !frame.isMain()) return
                articleReaderDebugLog(
                    "onLoadError url=$failedUrl errorCode=$errorCode errorText=$errorText",
                )
                reportError(errorText.ifBlank { "Failed to load article page." })
            }
        })

        client.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
            override fun onBeforePopup(
                browser: CefBrowser,
                frame: org.cef.browser.CefFrame,
                targetUrl: String,
                targetFrameName: String,
            ): Boolean {
                browser.loadURL(targetUrl)
                return true
            }
        })
    }

    private fun maybeInjectScript() {
        val browser = browser ?: return
        if (disposed.get() || !initialPageLoaded || !areParagraphsReady || injectionRequested) return

        injectionRequested = true

        if (isArticleRead) {
            // Skip JS for already-read articles; signal ready immediately so no spinner shows.
            onInjectionSucceeded()
            return
        }

        SwingUtilities.invokeLater {
            if (disposed.get()) return@invokeLater

            runCatching {
                val browserUrl = browser.url.ifBlank { resolvedInitialUrl ?: initialUrl }
                articleReaderDebugLog(
                    "injectScript url=$browserUrl paragraphs=${paragraphs.size} areParagraphsReady=$areParagraphsReady has novel=${paragraphs.count { it.isNovel }}"
                )
                browser.executeJavaScript(
                    buildArticleReaderInjectionScript(paragraphs, includeRateTracker = true),
                    browserUrl,
                    0,
                )
            }.onFailure { throwable ->
                injectionRequested = false
                reportError(throwable.message ?: "Failed to highlight article paragraphs.")
            }
        }
    }

    private fun handleBridgeMessage(
        request: String,
        callback: CefQueryCallback,
    ): Boolean {
        articleReaderDebugLog("cefQuery raw=$request")
        val payload = parseArticleReaderBridgeMessage(request) ?: return false
        articleReaderDebugLog("cefQuery parsedType=${payload.type}")

        when (payload.type) {
            "ready" -> {
                articleReaderDebugLog("cefQuery ready")
                onInjectionSucceeded()
                callback.success("")
                return true
            }

            "progress" -> {
                val value = payload.data?.value ?: return false
                articleReaderDebugLog("cefQuery progress value=$value")
                onProgressChanged(value.toFloat())
                callback.success("")
                return true
            }

            "rate" -> {
                val rateValue = payload.data?.rate ?: return false
                articleReaderDebugLog("cefQuery rate value=$rateValue")
                onRateArticle(rateValue)
                callback.success("")
                return true
            }

            else -> return false
        }
    }

    private fun reportError(message: String) {
        pendingErrors += message
        flushPendingErrors()
    }

    private fun flushPendingErrors() {
        if (pendingErrors.isEmpty()) return

        val errors = pendingErrors.toList()
        pendingErrors.clear()
        errors.forEach(onError)
    }

    private inner class ArticleReaderBridgeHandler : CefMessageRouterHandlerAdapter() {
        override fun onQuery(
            browser: CefBrowser,
            frame: org.cef.browser.CefFrame,
            queryId: Long,
            request: String,
            persistent: Boolean,
            callback: CefQueryCallback,
        ): Boolean = handleBridgeMessage(request, callback)
    }
}

private fun articleReaderDebugLog(message: String) {
    println("[ThulurArticleReader][JCEF] $message")
}

@kotlinx.serialization.Serializable
internal data class ArticleReaderBridgeMessage(
    val type: String,
    val data: ArticleReaderBridgeMessageData? = null,
)

@kotlinx.serialization.Serializable
internal data class ArticleReaderBridgeMessageData(
    val value: Double? = null,
    val rate: Int? = null,
)
