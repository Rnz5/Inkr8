package com.inkr8.data

data class Submissions(
    val id: Int,
    val userId: Int,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val wordsUsed: List<Words> = emptyList(),
    val gamemode: Gamemode,
    val topicId: Int? = null,
    val themeId: Int? = null,
    val evaluation: Evaluation? = null,
    val status: SubmissionStatus = SubmissionStatus.PENDING
)
