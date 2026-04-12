package com.inkr8.data

data class Submissions(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val evaluation: Evaluation? = null,
    val gamemode: String = "STANDARD",
    val playmode: String = "PRACTICE",
    val status: SubmissionStatus = SubmissionStatus.PENDING,
    val isSaved: Boolean = false,
    val wordsUsed: List<Words> = emptyList(),
    val topicId: String? = null,
    val themeId: String? = null
)
