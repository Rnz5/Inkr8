package com.inkr8.evaluation

import com.inkr8.data.Gamemode
import com.inkr8.data.OnTopicWriting
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.data.Words
import java.util.UUID

object SubmissionFactory {
    fun create(
        content: String,
        gamemode: String,
        wordsUsed: List<Words>,
        authorId: String = ""
    ): Submissions {
        val wordCount = content.split("\\s+".toRegex()).size
        return Submissions(
            id = UUID.randomUUID().toString(),
            authorId = authorId,
            content = content,
            timestamp = System.currentTimeMillis(),
            wordCount = wordCount,
            characterCount = content.length,
            wordsUsed = wordsUsed,
            gamemode = "gamemode",
            topicId = (gamemode as? OnTopicWriting)?.topic?.id as String?,
            themeId = (gamemode as? OnTopicWriting)?.theme?.id as String?,
            evaluation = null,
            status = SubmissionStatus.PENDING,
        )
    }
}