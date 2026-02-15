package com.inkr8.data

data class Submissions(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val wordsUsed: List<Words> = emptyList(),
    val gamemode: String = "",
    val topicId: String? = null,
    val themeId: String? = null,
    val evaluation: Evaluation? = null,
    val status: SubmissionStatus = SubmissionStatus.PENDING
)
