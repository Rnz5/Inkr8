package com.inkr8.economic

import com.inkr8.data.Topic
import com.inkr8.data.someThemes

object EconomyConfig{
    val show_example_sentence: Long = 25
    val change_pfp: Long = 500
    val change_banner: Long = 750


    fun insuffientMerit(): String{
        val phrases = listOf(
            "Not enough Merit",
            "There isn't enough Merit",
            "You need more Merit",
            "You ran out of Merit",
            "Search for more Merit"
        )
        return phrases.random()
    }
}


