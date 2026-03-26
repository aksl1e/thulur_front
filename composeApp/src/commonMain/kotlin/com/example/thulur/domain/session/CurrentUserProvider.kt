package com.example.thulur.domain.session

/**
 * Provides the current app-level user identifier used by authenticated calls.
 *
 */
interface CurrentUserProvider {
    /**
     * Returns the current user id to be sent to the backend.
     */
    fun currentUserId(): String
}
