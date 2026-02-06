package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Evaluation
import com.inkr8.data.StandardWriting
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.ui.theme.Inkr8Theme
val fakeSubmission = Submissions(
    id = 1,
    userId = 1,
    content = "Example text wrote by an insane user with several mental issues",
    wordCount = 42,
    characterCount = 210,
    wordsUsed = emptyList(),
    gamemode = StandardWriting,
    evaluation = Evaluation(submissionId = 1, finalScore = 87.5, feedback = "good vocab or whatever, i dont really care dude, hell", isExpanded = false, resultStatus = SubmissionStatus.EVALUATED),
    status = SubmissionStatus.EVALUATED
)
@Composable
fun Results(
    submission: Submissions,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
){
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Results",
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onNavigateToPractice
            ) {
                Text(
                    text = "Practice",
                )
            }

            Button(
                onClick = onNavigateBack,
            ) {
                Text("Back")
            }
        }

    }
}




@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultsPreview() {
    Inkr8Theme {
        Results(
            submission = fakeSubmission,
            onNavigateBack = {},
            onNavigateToPractice = {}
        )
    }
}