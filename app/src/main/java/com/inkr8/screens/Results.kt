package com.inkr8.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.ui.theme.Inkr8Theme

private val previewSubmission = Submissions(
    id = "preview-submission",
    authorId = "user123",
    content = "This is a preview paragraph written to test the visual structure of the new results screen and how the feedback section behaves before and after expansion. R8 is watching every keystroke, judging the structural integrity of your linguistic output.",
    timestamp = System.currentTimeMillis(),
    wordCount = 38,
    characterCount = 214,
    wordsUsed = emptyList(),
    gamemode = "STANDARD",
    evaluation = Evaluation(
        submissionId = "preview-submission",
        finalScore = 82.47,
        feedback = "Your grammar held together better than expected. The structure was clear, but the phrasing still lacked the kind of sharpness that would make R8 raise an eyebrow.",
        expandedFeedback = "Structural analysis complete. Your use of syntax is adequate but predictable. To reach elite status, you must abandon safe phrasing. The metrics indicate a high coherence score, yet your creativity index remains within common parameters. Refine your lexicon or remain forgotten in the archives.",
        isExpanded = false,
        feedbackUnlocked = false,
        resultStatus = SubmissionStatus.EVALUATED,
        meritEarned = 57
    ),
    status = SubmissionStatus.EVALUATED,
    playmode = "PRACTICE"
)

@Composable
fun Results(
    submission: Submissions,
    isUnlockingFeedback: Boolean,
    isPhilosopher: Boolean,
    onUnlockFeedback: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
) {
    val evaluation = submission.evaluation
        ?: return Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)), contentAlignment = Alignment.Center) {
            Text("R8 Judging...", color = Color.Gray, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }

    val isUnlockedByMerit = evaluation.feedbackUnlocked
    val isEffectivelyUnlocked = isUnlockedByMerit || isPhilosopher
    
    val feedbackToShow = if (isEffectivelyUnlocked) {
        evaluation.expandedFeedback ?: evaluation.feedback
    } else {
        val baseFeedback = evaluation.feedback
        val teaserLength = (baseFeedback.length * 0.45).toInt()
        val teaser = if (baseFeedback.length > teaserLength) {
            val lastSpace = baseFeedback.take(teaserLength).lastIndexOf(' ')
            if (lastSpace > 0) baseFeedback.take(lastSpace) + "..." else baseFeedback.take(teaserLength) + "..."
        } else {
            baseFeedback
        }
        teaser
    }

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundDark).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Verdict Delivered",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier.clip(CircleShape).background(Color(0xFF4CAF50).copy(alpha = 0.1f)).border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Synced", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.2f".format(evaluation.finalScore) + "%",
                fontSize = 64.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp
            )
            
            val appraisal = when {
                evaluation.finalScore >= 95 -> "GOD TIER. RARE PRECISION."
                evaluation.finalScore >= 90 -> "ELITE. SYSTEM ACKNOWLEDGED."
                evaluation.finalScore >= 80 -> "STRONG. ALMOST REFINED."
                evaluation.finalScore >= 70 -> "COMPETENT. STILL SAFE."
                evaluation.finalScore >= 60 -> "FINE. COMMON OUTPUT."
                else -> "WEAK. REWORK EVERYTHING."
            }
            
            Text(
                text = appraisal,
                color = primaryGold,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "R8 Breakdown",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!isEffectivelyUnlocked && submission.playmode == "PRACTICE") {
                        Button(
                            onClick = onUnlockFeedback,
                            enabled = !isUnlockingFeedback,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isUnlockingFeedback) "Decrypting..." else "Expand - 55",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (isUnlockedByMerit && !isPhilosopher) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(primaryGold.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Decrypted", color = primaryGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = feedbackToShow,
                    color = if (isEffectivelyUnlocked) Color.White else Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    fontStyle = if (isEffectivelyUnlocked) androidx.compose.ui.text.font.FontStyle.Normal else androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                text = "Transmission Log",
                color = Color.DarkGray,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = submission.content,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Merit Gain", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "+${evaluation.meritEarned}",
                        color = primaryGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.05f)))
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Lexicon", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${submission.wordCount} Words",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Back", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigateToPractice,
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("Practice Again", fontWeight = FontWeight.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
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
            onNavigateToPractice = {},
            isPhilosopher = false
        )
    }
}
