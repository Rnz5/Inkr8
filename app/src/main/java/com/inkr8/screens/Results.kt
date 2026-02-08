package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
val fakeSubmission = Submissions(
    id = "1",
    userId = "1",
    content = "The philosophy of spirit, famously championed by G.W.F. Hegel, examines the evolution of consciousness from individual awareness to a collective, universal realization. In this context, \"spirit\" (or Geist) represents the unity of thought and reality, bridging the gap between the subjective mind and the external world. Hegel’s system structures this journey into three stages: subjective spirit, involving individual psychology; objective spirit, encompassing law and societal ethics; and absolute spirit, which reaches its peak through art, religion, and philosophy.",
    wordCount = 150,
    characterCount = 610,
    wordsUsed = listOf(Words(
        0,
        "leeway",
        "noun",
        "The sideways drift of a ship or boat to leeward of the desired course",
        "/ˈliˌweɪ/",
        "By manœuvring the sheets it could be made to keep the boat moving and reduce leeway.",
        "Common"
    ),Words(
        1,
        "shorting",
        "noun",
        "The action of short",
        "/ˈʃɔrdɪŋ/",
        "The shorting for thy summer fruits and thy harvest is fallen.",
        "Common"
    ),Words(
        2,
        "bulletproof",
        "verb",
        "To make (something) bulletproof",
        "/ˈbʊlətˌpruf/",
        "To bulletproof your legal arguments, use the most reliable source for determining case validity.",
        "Common"
    ),
        Words(
            3,
            "causalism",
            "noun",
            "Any theory or approach ascribing particular importance to causes or causal relationships in understanding the nature of something.",
            "/ˈkɔzəˌlɪzəm/",
            "The doctrine of a motiveless volition would be only causalism.",
            "Rare"
        ),),
    gamemode = StandardWriting,
    evaluation = Evaluation(submissionId = "1", finalScore = 87.56, feedback = "While the summary accurately delineates the Hegelian triad, it remains descriptive rather than analytical, lacking a critical examination of the dialectical transitions between these stages. It is a technically sound overview but requires more depth regarding the \"negation\" that drives the movement of Geist to earn a top-tier grade.", isExpanded = false, resultStatus = SubmissionStatus.EVALUATED, meritEarned = 5),
    status = SubmissionStatus.EVALUATED
)
@Composable
fun Results(
    submission: Submissions,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
){
    val evaluation = submission.evaluation ?: return Text("Submission not evaluated yet")
    var isExpanded by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Results",
            textAlign = TextAlign.Center,
            fontSize = 48.sp,
            modifier = Modifier.padding(4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){
                    Text(
                        text = "Words: ${submission.wordCount} | Characters: ${submission.characterCount}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(10.dp),
                        color = Color.Red
                    )
                    val formatScore = "%.2f".format(submission.evaluation.finalScore)
                    Text(
                        text = "${formatScore}%",
                        textAlign = TextAlign.End,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(10.dp),
                        color = Color.Red
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = submission.content,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontSize = 13.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Feedback",
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Collapse Feedback" else "Expand Feedback")
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if(isExpanded){
                    Text(
                        text = submission.evaluation.feedback,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }else{
                    Text(
                        text = submission.evaluation.feedback.take(submission.evaluation.feedback.length/2) + "...",
                        modifier = Modifier.padding(horizontal = 4.dp),
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Merit Earned: ${submission.evaluation.meritEarned}",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 20.sp,
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
                Text(text = "Practice Again")
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