package com.inkr8.evaluation

import com.inkr8.data.Submissions

class SubmissionProcessor(
    private val evaluator: SubmissionEvaluator
){
    fun process(submission: Submissions): Submissions {
        val evaluation = evaluator.evaluate(submission)

        val evaluatedSubmission = submission.copy(
            evaluation = evaluation,
            status = evaluation.resultStatus
        )

        val merit = MeritCalculator.CalculateMerit(evaluatedSubmission)

        return evaluatedSubmission.copy(evaluation = evaluation.copy(meritEarned = merit)
        )
    }
}
