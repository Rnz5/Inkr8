package com.inkr8.evaluation

import com.inkr8.data.Submissions

class SubmissionProcessor(
    private val evaluator: SubmissionEvaluator
){
    fun process(submission: Submissions): Submissions {

        val evaluation = evaluator.evaluate(submission)
        val evaluatedSubmission = submission.copy(evaluation = evaluation, status = evaluation.resultStatus)

        val isRanked = evaluatedSubmission.playmode == "RANKED"

        val merit: Long = MeritCalculator.CalculateMerit(submission = evaluatedSubmission, isRanked = isRanked)

        return evaluatedSubmission.copy(
            evaluation = evaluation.copy(meritEarned = merit)
        )
    }
}
