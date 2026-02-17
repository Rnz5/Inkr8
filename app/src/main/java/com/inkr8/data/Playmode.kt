package com.inkr8.data

sealed class PlayMode {
    object Practice: PlayMode()
    object Ranked: PlayMode()

    data class Tournament(val tournamentId: String): PlayMode()
}