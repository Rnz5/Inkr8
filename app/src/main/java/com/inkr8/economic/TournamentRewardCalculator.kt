package com.inkr8.economic

object TournamentRewardCalculator {

    fun calculateRewards(prizePool: Long, players: Int): List<Double> {

        if (players <= 0) return emptyList()

        val rewards = MutableList(players) { 0.0 }

        val losersCount = (players * 0.2).toInt()
        val winnersCount = players - losersCount

        if (winnersCount <= 0) return rewards

        val baseTopPercent = 0.45

        val remainingPercent = 1.0 - baseTopPercent

        rewards[0] = baseTopPercent

        if (winnersCount == 1) return rewards

        val step = remainingPercent / (winnersCount - 1)

        for (i in 1 until winnersCount) {
            rewards[i] = step
        }

        return rewards
    }
}