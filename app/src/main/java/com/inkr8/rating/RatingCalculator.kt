package com.inkr8.rating

object RatingCalculator{

    fun calculateNewRating(
        currentRating: Long,
        score: Double,
        kFactor: Int = 32
    ): Long {
        val expectedScore = 60.0
        val delta = (score - expectedScore) / 100.0

        val newRating = currentRating + (kFactor * delta)

        return newRating.toLong().coerceAtLeast(0)
    }
}