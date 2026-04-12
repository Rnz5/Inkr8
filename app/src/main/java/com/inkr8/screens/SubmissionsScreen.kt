package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Submissions
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.TimeUtils.formatTime

@Composable
fun SubmissionsScreen(
    submissions: List<Submissions>,
    isLoading: Boolean,
    onNavigateToProfile: () -> Unit,
    onSaveSubmission: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Archive",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Writing History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Text("✕", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (submissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("The archive is empty. Begin your climb.", color = Color.Gray)
            }
        } else {
            val savedCount = submissions.count { it.isSaved }
            Text(
                text = "Archive Status: ${submissions.size - savedCount}/10 standard entries. $savedCount protected.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(submissions) { submission ->
                    SubmissionItem(submission, onSaveSubmission)
                }
            }
        }
    }
}

@Composable
fun SubmissionItem(submission: Submissions, onSaveSubmission: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = submission.gamemode,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatTime(submission.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
                val score = submission.evaluation?.finalScore ?: 0.0
                Text(
                    text = "%.2f".format(score)+"%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = when {
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = submission.content.let { if (it.length > 150) it.take(150) + "..." else it },
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTag("${submission.wordCount} words")
                    if (submission.isSaved) {
                        Box(
                            modifier = Modifier.background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Protected", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDAA520))
                        }
                    }
                }

                if (!submission.isSaved) {
                    TextButton(
                        onClick = { onSaveSubmission(submission.id) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Save - 2000 Merit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoTag(text: String) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun SubmissionsPreview() {
    Inkr8Theme {
        SubmissionsScreen(submissions = emptyList(), isLoading = false, onNavigateToProfile = {}, onSaveSubmission = {})
    }
}