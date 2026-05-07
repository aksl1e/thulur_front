package com.example.thulur.presentation.dailyfeed.article_reader

import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import org.cef.CefApp
import org.cef.CefClient
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

internal object JcefBrowserRuntime {
    private val initExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "thulur-jcef-init").apply {
            isDaemon = true
        }
    }
    private val initFutureRef = AtomicReference<CompletableFuture<Result<CefApp>>?>()

    fun prewarm() {
        startInitialization()
    }

    fun shutdown() {
        val future = initFutureRef.get() ?: return
        if (!future.isDone) return
        future.getNow(null)?.getOrNull()?.dispose()
    }

    fun createClientAsync(): CompletableFuture<Result<CefClient>> = startInitialization()
        .thenApply { result ->
            if (result.isSuccess) {
                runCatching {
                    result.getOrThrow().createClient()
                }
            } else {
                Result.failure(result.exceptionOrNull() ?: IllegalStateException("JCEF initialization failed."))
            }
        }

    private fun startInitialization(): CompletableFuture<Result<CefApp>> {
        initFutureRef.get()?.let { return it }

        synchronized(initFutureRef) {
            initFutureRef.get()?.let { return it }

            val future = CompletableFuture.supplyAsync(
                {
                    runCatching {
                        buildCefApp()
                    }
                },
                initExecutor,
            )
            initFutureRef.set(future)
            return future
        }
    }

    private fun buildCefApp(): CefApp {
        val runtimeDir = defaultJcefRuntimeDir().also(File::mkdirs)
        val rootCacheDir = File(runtimeDir, "root-cache").also(File::mkdirs)
        val cacheDir = File(rootCacheDir, "cache").also(File::mkdirs)
        val installDir = File(runtimeDir, "bundle").also(File::mkdirs)

        val builder = CefAppBuilder()
        builder.setInstallDir(installDir)
        builder.addJcefArgs("--disable-gpu")
        builder.getCefSettings().apply {
            windowless_rendering_enabled = false
            cache_path = cacheDir.absolutePath
            root_cache_path = rootCacheDir.absolutePath
            log_file = File(runtimeDir, "jcef.log").absolutePath
        }
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {})

        return builder.build()
    }
}

private fun defaultJcefRuntimeDir(): File = File(
    File(System.getProperty("user.home"), ".thulur"),
    "jcef-runtime",
)
