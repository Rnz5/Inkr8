package com.inkr8.economic

import kotlin.math.pow

object TournamentRewardCalculator {

    fun calculateRewardPercentages(players: Int): List<Double> {

        if (players == 1) {
            return listOf(1.0)
        }

        if (players <= 0) return emptyList()

        val losersCount = (players * 0.2).toInt()
        val winnersCount = players - losersCount

        if (winnersCount <= 0) return List(players) { 0.0 }

        val result = MutableList(players) { 0.0 }

        val top1Percent = 0.45
        result[0] = top1Percent

        if (winnersCount == 1) return result

        val exponent = 1.4  //<- this determines the steepness of the curve, it might change

        val weights = mutableListOf<Double>()

        for (i in 2..winnersCount) {
            val weight = 1.0 / i.toDouble().pow(exponent)
            weights.add(weight)
        }

        val totalWeight = weights.sum()

        val remainingPercent = 1.0 - top1Percent

        for (i in weights.indices) { //decreasing curve values
            val normalized = (weights[i] / totalWeight) * remainingPercent
            result[i + 1] = normalized
        }

        return result
    }
}