package com.inkr8.economy

data class TournamentEconomyProjection(
    val prizePool: Long,
    val maxPlayers: Int,
    val entranceFee: Long,
    val totalRevenue: Long,
    val systemFee: Long,
    val netProfit: Long,
    val breakEvenPlayers: Int
)