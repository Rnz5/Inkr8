package com.inkr8.evaluation

import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import kotlin.random.Random

class FakeEvaluator: SubmissionEvaluator {
    override fun evaluate(submission: Submissions): Evaluation {
        val createdEvaluation = Evaluation(
            submissionId = "1",
            finalScore = Random.nextDouble()*100,
            feedback = "you did great but let me tell you that R8 might have done it better noob, so yeah go and try to find a job silly, but i am already doing that, shut up you are just an insane person probably also squizo",
            isExpanded = false,
            resultStatus = SubmissionStatus.EVALUATED
        )
        return createdEvaluation
    }
}