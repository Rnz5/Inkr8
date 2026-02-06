package com.inkr8.evaluation

import com.inkr8.data.Evaluation
import com.inkr8.data.Submissions

interface SubmissionEvaluator {
    fun evaluate(submission: Submissions): Evaluation

}