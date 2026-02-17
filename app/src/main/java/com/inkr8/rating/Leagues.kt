package com.inkr8.rating
enum class League(val minRating: Int) {
    SCRIBE(0),
    STYLIST(30),
    AUTHOR(60),
    NOVELIST(90),
    LAUREATE(120),
    LUMINARY(150);

    companion object {
        fun fromRating(rating: Long): League {
            return entries.last { rating >= it.minRating }
        }
    }

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}



