package com.inkr8.rating

import com.inkr8.data.Users

object PantheonManager {

    const val MIN_RATING = 180

    fun checkPantheonStatus(currentUser: Users, top100: List<Users>): Pair<Boolean, Int?> {

        if (currentUser.rating < MIN_RATING) {
            return false to null
        }

        val sorted = top100.sortedByDescending { it.rating }

        val position = sorted.indexOfFirst { it.id == currentUser.id }

        return if (position in 0..99) {
            true to (position + 1)
        } else {
            false to null
        }
    }
}
