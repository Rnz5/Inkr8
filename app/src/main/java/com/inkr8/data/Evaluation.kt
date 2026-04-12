package com.inkr8.data

data class Evaluation(
    val submissionId: String? = "",
    val finalScore: Double = 0.0,
    val meritEarned: Long = 0,
    val ratingChange: Long = 0,
    val sarcasm: String = "",
    val feedback: String = "",
    val expandedFeedback: String? = null,
    val feedbackUnlocked: Boolean = false,
    val isExpanded: Boolean = false,
    val resultStatus: SubmissionStatus = SubmissionStatus.PENDING,
    val rankLeaderboard: Int? = null,
    val metrics: Map<String, Double> = emptyMap()
)
