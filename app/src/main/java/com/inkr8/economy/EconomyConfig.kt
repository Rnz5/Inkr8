package com.inkr8.economy

object EconomyConfig{

    //Basics
    const val SHOW_EXAMPLE_SENTENCE: Long = 25
    const val CHANGE_PFP: Long = 500
    const val CHANGE_BANNER: Long = 750

    //Ranked mode
    const val BASE_COST_RANKED: Long = 100

    // Tournament
    const val SYSTEM_CREATION_FEE_PERCENT: Double = 0.034 //3.4% of prize pool for creation of tournament
    const val PROFIT_MARGIN_PERCENT: Double = 0.12 // 12% target profit


    fun insufficientMerit(): String{
        val phrases = listOf(
            "Not enough Merit",
            "There isn't enough Merit",
            "You need more Merit",
            "You ran out of Merit",
            "Search for more Merit",
            "Insufficient Merit"
        )
        return phrases.random()
    }
}


