package com.inkr8.evaluation

import com.inkr8.data.OnTopicWriting
import com.inkr8.data.Submissions

object MeritCalculator {

    fun CalculateMerit(submission: Submissions, isRanked: Boolean): Long {
        val evaluation = submission.evaluation ?: return 0
        val baseMerit: Double
        val wordBonusMerit: Double
        val gamemodeMerit: Double

        val finalScore = submission.evaluation.finalScore
        val modeMultiplier = if (isRanked) 1.0 else 0.4

        if(finalScore >= 90){
            baseMerit = 400.0
        }else if(finalScore >= 80){
            baseMerit = 300.0
        }else if(finalScore >= 70){
            baseMerit = 220.0
        }else if(finalScore >= 60){
            baseMerit = 150.0
        }else if(finalScore >= 50){
            baseMerit = 80.0
        }else if(finalScore >= 30){
            baseMerit = 40.0
        }else if(finalScore >= 10){
            baseMerit = 20.0
        }else if(finalScore >= 5){
            baseMerit = 10.0
        }else{
            baseMerit = 0.0
        }

        wordBonusMerit = (submission.wordCount / 30) * 20.0 //for each 30 words == 20 merit
        if(submission.gamemode == "ON_TOPIC"){
            gamemodeMerit = 1.3
        }else{
            gamemodeMerit = 1.1
        }


        return (((baseMerit + wordBonusMerit) * gamemodeMerit) * modeMultiplier).toLong()
    }
}