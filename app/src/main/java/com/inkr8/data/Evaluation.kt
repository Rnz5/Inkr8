package com.inkr8.data

data class Evaluation(
    val submissionId: String? = "",
    val finalScore: Double = 0.0,
    val feedback: String = "",
    val isExpanded: Boolean = false,
    val resultStatus: SubmissionStatus = SubmissionStatus.PENDING,
    val meritEarned: Long = 0,
    val rankLeaderboard: Long = 0
)
