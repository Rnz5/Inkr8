package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Evaluation
import com.inkr8.data.StandardWriting
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.data.Words
import com.inkr8.ui.theme.Inkr8Theme

private val previewSubmission = Submissions(
    id = "preview-submission",
    authorId = "user123",
    content = "This is a preview paragraph written to test the visual structure of the new results screen and how the feedback section behaves before and after expansion.",
    timestamp = System.currentTimeMillis(),
    wordCount = 28,
    characterCount = 164,
    wordsUsed = emptyList(),
    gamemode = "STANDARD",
    topicId = null,
    themeId = null,
    evaluation = Evaluation(
        submissionId = "preview-submission",
        finalScore = 82.47,
        feedback = "Your grammar held together better than expected, which is always a pleasant surprise. The structure was clear, but the phrasing still lacked the kind of sharpness that would make R8 raise an eyebrow in genuine respect.",
        isExpanded = false,
        resultStatus = SubmissionStatus.EVALUATED,
        meritEarned = 57,
        rankLeaderboard = 0
    ),
    status = SubmissionStatus.EVALUATED,
    playmode = "PRACTICE"
)

@Composable
fun Results(
    submission: Submissions,
    isUnlockingFeedback: Boolean,
    onUnlockFeedback: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
) {

    val evaluation = submission.evaluation
        ?: return Text("R8 is still judging...")
    val isExpanded = evaluation.isExpanded
    val isPracticeSubmission = submission.playmode == "PRACTICE"
    val collapsedFeedback = if (evaluation.feedback.length > 140) {
        evaluation.feedback.take(140).trimEnd() + "..."
    } else {
        evaluation.feedback
    }


    val formattedScore = "%.2f".format(evaluation.finalScore)

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "R8 has judged your writing",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "$formattedScore%",
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        evaluation.finalScore >= 90 -> "Finally. Something worth reading."
                        evaluation.finalScore >= 75 -> "Decent. Not memorable."
                        evaluation.finalScore >= 60 -> "Acceptable, barely."
                        else -> "You can do better. Much better."
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "R8 Analysis",
                        fontSize = 18.sp
                    )

                    if (isPracticeSubmission && !isExpanded) {
                        Button(
                            onClick = onUnlockFeedback,
                            enabled = !isUnlockingFeedback
                        ) {
                            Text(
                                if (isUnlockingFeedback) "Unlocking..."
                                else "Expand -55 Merit"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isExpanded) evaluation.feedback else collapsedFeedback,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Your Writing",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = submission.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "+${evaluation.meritEarned} Merit",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (evaluation.meritEarned > 0)
                        "You gained ground."
                    else
                        "No progress.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Words: ${submission.wordCount} • Characters: ${submission.characterCount}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Button(onClick = onNavigateToPractice) {
                Text("Practice Again")
            }

            Button(onClick = onNavigateBack) {
                Text("Home")
            }
        }
    }
}




@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultsPreview() {
    Inkr8Theme {
        Results(
            submission = previewSubmission,
            isUnlockingFeedback = false,
            onUnlockFeedback = {},
            onNavigateBack = {},
            onNavigateToPractice = {}
        )
    }
}