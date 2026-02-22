package com.inkr8.rating

object ReputationManager {
    const val MAX_REPUTATION = 1000L
    const val MIN_REPUTATION = -1000L

    fun adjustReputation(current: Long, delta: Long): Long {

        val raw = current + delta

        val clamped = when {
            raw > MAX_REPUTATION -> MAX_REPUTATION
            raw < MIN_REPUTATION -> MIN_REPUTATION
            else -> raw
        }

        if (current != 0L && clamped == 0L) {
            return if (delta > 0) 1L else -1L
        }

        return clamped
    }

    fun onRankedCompleted(current: Long): Long {
        return adjustReputation(current, +3)
    }

    fun onRankedAbandoned(current: Long): Long {
        return adjustReputation(current, -12)
    }

    fun consistencyBonus(current: Long): Long {
        return adjustReputation(current, +8)
    }

    fun dailyDrift(current: Long): Long {
        return when {
            current > 0 -> adjustReputation(current, -1)
            current < 0 -> adjustReputation(current, +1)
            else -> 0L
        }
    }
}
