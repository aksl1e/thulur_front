package com.example.thulur.domain.auth

import com.example.thulur.data.auth.BrowserPasskeyAuthenticator
import com.example.thulur_api.ThulurApi

actual fun providePlatformPasskeyAuthenticator(thulurApi: ThulurApi): PasskeyAuthenticator =
    BrowserPasskeyAuthenticator(thulurApi = thulurApi)
