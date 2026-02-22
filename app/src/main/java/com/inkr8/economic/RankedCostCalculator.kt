package com.inkr8.economic

object RankedCostCalculator{

    fun calculateCost(
        baseCost: Long,
        winStreak: Long,
        lossStreak: Long,
        reputation: Long
    ): Long{

        var modifier = 1.0

        if(winStreak > 0){
            modifier += (winStreak*0.05).coerceAtMost(0.75)
        }
        if(lossStreak > 0){
            modifier -= (lossStreak*0.05).coerceAtMost(0.4)
        }

        val repModifier = when {
            reputation >= 900 -> 0.80
            reputation >= 700 -> 0.88
            reputation >= 400 -> 0.94
            reputation >= 200 -> 0.97

            reputation <= -900 -> 1.40
            reputation <= -700 -> 1.30
            reputation <= -400 -> 1.20
            reputation <= -200 -> 1.12

            else -> 1.0
        }

        modifier *= repModifier


        return (baseCost*modifier).toLong().coerceAtLeast(1)
    }

}