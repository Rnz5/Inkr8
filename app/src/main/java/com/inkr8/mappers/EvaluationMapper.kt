package com.inkr8.mappers

import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.repository.FirestoreEvaluation

fun FirestoreEvaluation.toDomain(): Evaluation {
    return Evaluation(
        submissionId = submissionId ?: "",
        finalScore = finalScore,
        feedback = feedback,
        isExpanded = expanded,
        resultStatus = SubmissionStatus.valueOf(
            resultStatus.ifBlank { "PENDING" }
        ),
        meritEarned = meritEarned.toLong()
    )
}

fun Evaluation.toFirestore(): FirestoreEvaluation {
    return FirestoreEvaluation(
        submissionId = submissionId,
        finalScore = finalScore,
        feedback = feedback,
        expanded = isExpanded,
        resultStatus = resultStatus.name,
        meritEarned = meritEarned.toLong()
    )
}