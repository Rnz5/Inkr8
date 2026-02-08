package com.inkr8.evaluation

import com.inkr8.data.OnTopicWriting
import com.inkr8.data.Submissions

object MeritCalculator {

    fun CalculateMerit(submission: Submissions): Int {
        val evaluation = submission.evaluation ?: return 0
        val baseMerit: Double
        val wordBonusMerit: Double
        val gamemodeMerit: Double

        val finalScore = submission.evaluation.finalScore

        if(finalScore >= 90){
            baseMerit = 1500.0
        }else if(finalScore >= 80){
            baseMerit = 1300.0
        }else if(finalScore >= 70){
            baseMerit = 1100.0
        }else if(finalScore >= 60){
            baseMerit = 900.0
        }else if(finalScore >= 50){
            baseMerit = 700.0
        }else if(finalScore >= 30){
            baseMerit = 500.0
        }else if(finalScore >= 10){
            baseMerit = 200.0
        }else if(finalScore >= 5){
            baseMerit = 50.0
        }else{
            baseMerit = 0.0
        }

        wordBonusMerit = (submission.wordCount / 30) * 50.toDouble()
        if(submission.gamemode is OnTopicWriting){
            gamemodeMerit = 1.3
        }else{
            gamemodeMerit = 1.1
        }


        return ((baseMerit + wordBonusMerit) * gamemodeMerit).toInt()
    }

}