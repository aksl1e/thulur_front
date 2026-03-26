package com.example.thulur.domain.repository

import com.example.thulur.domain.model.MainFeedThread
import kotlinx.datetime.LocalDate

/**
 * App data boundary over the Thulur API module.
 *
 * The repository hides transport details from features and exposes
 * app-oriented models instead of raw DTOs.
 */
interface ThulurApiRepository {
    /**
     * Loads the Main Feed for the current user.
     *
     * @param day Optional day filter. When `null`, backend default behavior is used.
     */
    suspend fun getMainFeed(day: LocalDate? = null): List<MainFeedThread>
}
