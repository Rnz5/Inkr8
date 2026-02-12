package com.inkr8.mappers

import com.inkr8.data.*
import com.inkr8.repository.FirestoreEvaluation
import com.inkr8.repository.FirestoreSubmission

fun FirestoreSubmission.toDomain(): Submissions {
    return Submissions(
        id = id,
        authorId = authorId,
        content = content,
        timestamp = timestamp,
        wordCount = wordCount,
        characterCount = characterCount,
        wordsUsed = wordsUsed,
        gamemode = gamemodeName,
        topicId = topicId,
        themeId = themeId,
        evaluation = evaluation?.let {
            Evaluation(
                submissionId = it.submissionId,
                finalScore = it.finalScore,
                feedback = it.feedback,
                meritEarned = it.meritEarned
            )
        },
        status = status
    )
}

fun Submissions.toFirestore(): FirestoreSubmission {
    return FirestoreSubmission(
        id = id,
        authorId = authorId,
        content = content,
        timestamp = timestamp,
        wordCount = wordCount,
        characterCount = characterCount,
        wordsUsed = wordsUsed,
        gamemodeName = gamemode,
        topicId = topicId,
        themeId = themeId,
        evaluation = evaluation?.let {
            FirestoreEvaluation(
                finalScore = it.finalScore,
                feedback = it.feedback,
                meritEarned = it.meritEarned
            )
        },
        status = status
    )
}