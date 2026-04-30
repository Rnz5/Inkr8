package com.inkr8.utils

object FormatUtils {
    fun formatPlace(place: Int): String {
        return when (place) {
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            else -> "${place}th"
        }
    }
}
