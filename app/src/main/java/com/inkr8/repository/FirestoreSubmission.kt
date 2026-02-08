package com.inkr8.repository

import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Words

data class FirestoreSubmission(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val wordsUsed: List<Words> = emptyList(),
    val gamemodeName: String = "",
    val topicId: String? = null,
    val themeId: String? = null,
    val evaluation: Evaluation? = null,
    val status: SubmissionStatus = SubmissionStatus.PENDING
)