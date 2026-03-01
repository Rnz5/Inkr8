package com.inkr8.evaluation

import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import kotlin.random.Random

class R8Evaluator : SubmissionEvaluator {

    override fun evaluate(submission: Submissions): Evaluation {

        // gpt-4o mini procedure belongs here, no one should take its sit o_o

        return Evaluation(
            submissionId = submission.id,
            finalScore = Random.nextDouble() * 100,
            feedback = "R8 evaluated this, lol",
            resultStatus = SubmissionStatus.EVALUATED
        )
    }
}