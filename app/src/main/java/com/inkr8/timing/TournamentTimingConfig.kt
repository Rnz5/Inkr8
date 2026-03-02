package com.inkr8.timing

object TournamentTimingConfig {

    // enrollment lasts 24h
    const val ENROLLMENT_DURATION_MS = 24 * 60 * 60 * 1000L

    // competition lasts 24h after enrollment closes
    const val SUBMISSION_DURATION_MS = 24 * 60 * 60 * 1000L

    // minimum players required, it might change
    const val MIN_PLAYERS = 10
}