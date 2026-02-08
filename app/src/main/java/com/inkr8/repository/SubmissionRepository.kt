package com.inkr8.repository

import com.inkr8.data.Submissions

class SubmissionRepository {

    private val submissions = mutableListOf<Submissions>()
    private var lastSubmission: Submissions? = null

    fun addSubmission(submission: Submissions) {
        submissions.add(submission)
        lastSubmission = submission
    }

    fun getAllSubmissions(): List<Submissions> {
        return submissions
    }

    fun getLastSubmission(): Submissions? {
        return lastSubmission
    }

    fun clear() {
        submissions.clear()
        lastSubmission = null
    }
}
