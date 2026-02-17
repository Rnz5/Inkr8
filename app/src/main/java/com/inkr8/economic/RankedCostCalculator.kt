package com.inkr8.economic

object RankedCostCalculator{

    fun calculateCost(
        baseCost: Long,
        winStreak: Long,
        lossStreak: Long
    ): Long{

        var modifier = 1.0

        if(winStreak > 0){
            modifier += (winStreak*0.05).coerceAtMost(0.5)
        }
        if(lossStreak > 0){
            modifier -= (lossStreak*0.05).coerceAtMost(0.4)
        }

        return (baseCost*modifier).toLong().coerceAtLeast(1)
    }

}