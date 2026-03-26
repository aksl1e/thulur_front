package com.example.thulur.data.session

import com.example.thulur.domain.session.CurrentUserProvider

/**
 * Temporary DEVELOPMENT-ONLY implementation used until authentication lands.
 */
class DevCurrentUserProvider : CurrentUserProvider {
    override fun currentUserId(): String = DEV_USER_ID

    private companion object {
        // TODO: update after auth completion.
        const val DEV_USER_ID: String = "ff78144c-872e-41f7-89c5-d6cef0c63450"
    }
}
