package com.inkr8.data

data class Tournament(
    val id: String = "",
    val creatorId: String = "",
    val prizePool: Long = 0L,
    val maxPlayers: Long = 0L,
    val minPlayers: Long = 5L,
    val playersCount: Long = 0L,
    val entryFee: Long = 0L,
    val deadline: Long = 0L,
    val submissionsCount: Long = 0L,
    val requirements: TournamentRequirements = TournamentRequirements(),
    val status: TournamentStatus = TournamentStatus.OPEN,
    val strictnessMultiplier: Double = 0.92,
    val createdAt: Long = System.currentTimeMillis()
)

data class TournamentRequirements(
    val minRating: Int? = null,
    val maxRating: Int? = null,
    val minReputation: Int? = null,
    val minMerit: Long? = null
)

enum class TournamentStatus {
    OPEN,
    FULL,
    EVALUATING,
    COMPLETED,
    CANCELLED
}
