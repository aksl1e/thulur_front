package com.example.thulur.domain.session

import com.example.thulur.data.session.InMemorySecureTokenStore
import com.example.thulur.data.session.JvmSecureTokenStore

actual fun providePlatformSecureTokenStore(): SecureTokenStore =
    runCatching {
        JvmSecureTokenStore.createOrNull()
    }.getOrNull() ?: InMemorySecureTokenStore()
