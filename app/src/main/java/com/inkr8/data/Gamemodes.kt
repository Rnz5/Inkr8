package com.inkr8.data

sealed class Gamemodes {

    //Standard Writing - 150 words - 4 random words

    object StandardWriting: Gamemodes(){
        val maxWordCount = 150
        val timeLimit = 10

    }

    //On-Topic Writing - 200 words - 4 random words - specific topic

    data class OnTopicWriting(val topicId: String): Gamemodes(){
        val maxWordCount = 200
        val timeLimit = 15

    }

}
