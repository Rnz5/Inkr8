package com.inkr8.data

sealed class Gamemode(
    open val name: String,
    open val description: String,
    open val requiredWords: Int?,
    open val timeLimit: Int?,
    open val minWords: Int?,
    open val maxWords: Int?,
    open val topicId: Int?,
    open val themeId: Int?
)


object standardWriting: Gamemode(
    name = "Standard - Writing",
    description = "Write a 150-word paragraph using 4 random words",
    requiredWords = 4,
    timeLimit = null,
    minWords = 50,
    maxWords = 150,
    topicId = null,
    themeId = null
)


data class OnTopicWriting(val theme: Theme, val topic: Topic) : Gamemode(
    name = "On-Topic Writing",
    description = "Write a 200-word paragraph about a topic",
    requiredWords = 2,
    timeLimit = null,
    minWords = 50,
    maxWords = 200,
    topicId = topic.id,
    themeId = theme.id
)