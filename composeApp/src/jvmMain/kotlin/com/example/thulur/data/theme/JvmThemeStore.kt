package com.example.thulur.data.theme

import com.example.thulur.domain.theme.ThemeStore
import java.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JvmThemeStore : ThemeStore {
    private val prefs = Preferences.userRoot().node("com/example/thulur")

    override suspend fun readDarkMode(): Boolean? = withContext(Dispatchers.IO) {
        val raw = prefs.get(KEY_DARK_MODE, null) ?: return@withContext null
        raw == "true"
    }

    override suspend fun writeDarkMode(darkMode: Boolean) = withContext(Dispatchers.IO) {
        prefs.put(KEY_DARK_MODE, darkMode.toString())
        prefs.flush()
    }

    companion object {
        private const val KEY_DARK_MODE = "darkMode"
    }
}
