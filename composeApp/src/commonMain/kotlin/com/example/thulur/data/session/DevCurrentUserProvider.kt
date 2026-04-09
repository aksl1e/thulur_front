package com.example.thulur.data.session

import com.example.thulur.domain.session.CurrentUserProvider

/**
 * Temporary DEVELOPMENT-ONLY implementation used until authentication lands.
 */
class DevCurrentUserProvider : CurrentUserProvider {
    override fun currentUserId(): String = DEV_USER_ID

    private companion object {
        // TODO: update after auth completion.
        const val DEV_USER_ID: String = "a1274e96-c7ad-42b6-af46-c5d5a21f3cc2"
    }
}
