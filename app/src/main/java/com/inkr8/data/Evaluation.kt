package com.inkr8.data

data class Evaluation(
    val submissionId: Int,
    val finalScore: Double,
    val feedback: String,
    var isExpanded: Boolean,
    val resultStatus: SubmissionStatus
)
