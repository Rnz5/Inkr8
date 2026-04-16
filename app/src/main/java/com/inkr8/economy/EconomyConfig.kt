package com.inkr8.economy

object EconomyConfig{

    //Basics
    const val SHOW_EXAMPLE_SENTENCE: Long = 25
    const val PURCHASE_REPUTATION_VIEW: Long = 500
    const val CHANGE_USERNAME: Long = 1000
    const val CHANGE_PFP: Long = 500
    const val CHANGE_BANNER: Long = 750

    //Ranked mode
    const val BASE_COST_RANKED: Long = 100

    // Tournament
    const val SYSTEM_CREATION_FEE_PERCENT: Double = 0.034 //3.4% of prize pool for creation of tournament
    const val PROFIT_MARGIN_PERCENT: Double = 0.12 // 12% target profit

    // Submissions
    const val BASE_SAVE_SUBMISSION_COST: Long = 2000
    const val SAVE_COST_INCREMENT: Long = 200
    const val INCREMENT_THRESHOLD: Int = 3

    fun getSaveSubmissionCost(currentSavedCount: Int): Long {
        val increments = currentSavedCount / INCREMENT_THRESHOLD
        return BASE_SAVE_SUBMISSION_COST + (increments * SAVE_COST_INCREMENT)
    }

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
