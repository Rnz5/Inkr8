package com.inkr8.mappers

import com.inkr8.data.*
import com.inkr8.repository.FirestoreSubmission

fun FirestoreSubmission.toDomain(): Submissions {
    val gamemode = when (gamemodeName) {
        "STANDARD" -> StandardWriting
        "ON_TOPIC" -> OnTopicWriting(
            theme = Theme(
                id = themeId ?: "",
                name = "",
                description = "",
                difficulty = ""
            ),
            topic = Topic(
                id = topicId ?: "",
                themeId = themeId ?: "",
                name = "",
                description = "",
                difficulty = ""
            )
        )
        else -> StandardWriting
    }

    return Submissions(
        id = id,
        userId = userId,
        content = content,
        timestamp = timestamp,
        wordCount = wordCount,
        characterCount = characterCount,
        wordsUsed = wordsUsed,
        gamemode = gamemode,
        topicId = topicId,
        themeId = themeId,
        evaluation = evaluation,
        status = status
    )
}

fun Submissions.toFirestore(): FirestoreSubmission {
    return FirestoreSubmission(
        id = id,
        userId = userId,
        content = content,
        timestamp = timestamp,
        wordCount = wordCount,
        characterCount = characterCount,
        wordsUsed = wordsUsed,
        gamemodeName = when (gamemode) {
            is StandardWriting -> "STANDARD"
            is OnTopicWriting -> "ON_TOPIC"
        },
        topicId = topicId,
        themeId = themeId,
        evaluation = evaluation,
        status = status
    )
}