package com.inkr8.economy

import kotlin.math.ceil

object TournamentEconomyCalculator {

    fun calculateProjection(prizePool: Long, maxPlayers: Int): TournamentEconomyProjection {

        require(prizePool > 0) { "Prize pool must be positive" }
        require(maxPlayers > 1) { "At least 2 players required" }

        val systemFee = (prizePool * EconomyConfig.SYSTEM_CREATION_FEE_PERCENT).toLong()

        val targetRevenue = (prizePool * (1 + EconomyConfig.PROFIT_MARGIN_PERCENT)).toLong()

        val entranceFee = ceil(targetRevenue.toDouble() / maxPlayers).toLong()

        val totalRevenue = entranceFee * maxPlayers

        val netProfit = totalRevenue - prizePool - systemFee

        val breakEvenPlayers = ceil((prizePool + systemFee).toDouble() / entranceFee).toInt()

        return TournamentEconomyProjection(
            prizePool = prizePool,
            maxPlayers = maxPlayers,
            entranceFee = entranceFee,
            totalRevenue = totalRevenue,
            systemFee = systemFee,
            netProfit = netProfit,
            breakEvenPlayers = breakEvenPlayers
        )
    }
}