package com.inkr8.repository

data class FirestoreEvaluation(
    val submissionId: String? = null,
    val finalScore: Double = 0.0,
    val feedback: String = "",
    val expanded: Boolean = false,
    val resultStatus: String = "",
    val meritEarned: Long = 0
)