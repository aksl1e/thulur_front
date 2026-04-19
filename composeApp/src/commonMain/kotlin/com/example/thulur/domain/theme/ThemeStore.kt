package com.example.thulur.domain.theme

interface ThemeStore {
    suspend fun readDarkMode(): Boolean?
    suspend fun writeDarkMode(darkMode: Boolean)
}

expect fun providePlatformThemeStore(): ThemeStore
