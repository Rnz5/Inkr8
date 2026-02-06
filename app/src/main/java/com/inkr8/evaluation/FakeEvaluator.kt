package com.inkr8.evaluation

import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import kotlin.random.Random

class FakeEvaluator: SubmissionEvaluator {
    override fun evaluate(submission: Submissions): Evaluation {
        val createdEvaluation = Evaluation(
            submissionId = 1,
            finalScore = Random.nextDouble()*100,
            feedback = "you did great but let me tell you that R8 might have done it better noob",
            isExpanded = false,
            resultStatus = SubmissionStatus.EVALUATED
        )
        return createdEvaluation
    }
}