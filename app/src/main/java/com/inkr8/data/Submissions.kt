package com.inkr8.data

data class Submissions(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val score: Int = 0,
    val wordsUsed: List<String> = emptyList(),
    val topicId: String? = null
)
