package com.inkr8.rating

import com.inkr8.data.Users

object PantheonManager {

    const val MIN_RATING = 180L

    fun checkPantheonStatus(
        user: Users,
        top100: List<Users>
    ): Pair<Boolean, Int?> {

        if (user.rating < MIN_RATING) {
            return Pair(false, null)
        }

        val index = top100.indexOfFirst { it.id == user.id }

        return if (index != -1) {
            Pair(true, index + 1)
        } else {
            Pair(false, null)
        }
    }
}
