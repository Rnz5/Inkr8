package com.inkr8.data

import com.inkr8.timing.TournamentTimingConfig

data class Tournament(
    val id: String = "",
    val title: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val prizePool: Long = 0L,
    val maxPlayers: Long = 0L,
    val minPlayers: Long = TournamentTimingConfig.MIN_PLAYERS.toLong(),
    val playersCount: Long = 0L,
    val submissionsCount: Long = 0L,
    val entranceFee: Long = 0L,
    val systemFee: Long = 0L,
    val enrollmentDeadline: Long = 0L,
    val submissionDeadline: Long = 0L,
    val refunded: Boolean = false,
    val requirements: TournamentRequirements = TournamentRequirements(),
    val status: TournamentStatus = TournamentStatus.ENROLLING,
    val strictnessMultiplier: Double = 0.92,
    val createdAt: Long = System.currentTimeMillis(),
    val gamemode: String = "STANDARD",
    val isSystemHosted: Boolean = false,

    val requiredWords: List<String> = emptyList(),

    val themeId: String? = null,
    val themeName: String? = null,
    val topicId: String? = null,
    val topicName: String? = null
)

data class TournamentRequirements(
    val minRating: Int? = null,
    val maxRating: Int? = null,
    val minReputation: Int? = null,
    val minMerit: Long? = null
)

enum class TournamentStatus {
    ENROLLING,     // users can join
    ACTIVE,        // submissions allowed
    EVALUATING,    // R8 evaluation
    COMPLETED,     // Leaderboard screen and rewards repartition for enrolled users
    CANCELLED      // sad ending :(
}
