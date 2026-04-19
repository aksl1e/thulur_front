package com.example.thulur.domain.theme

import com.example.thulur.data.theme.JvmThemeStore

actual fun providePlatformThemeStore(): ThemeStore = JvmThemeStore()
