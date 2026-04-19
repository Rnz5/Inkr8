package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.inkr8.data.Submissions
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.TimeUtils.formatTime

@Composable
fun SavedSubmissionsScreen(
    savedSubmissions: List<Submissions>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onDeleteSubmission: (String) -> Unit
) {
    var selectedSubmission by remember { mutableStateOf<Submissions?>(null) }
    var submissionToDelete by remember { mutableStateOf<Submissions?>(null) }

    if (selectedSubmission != null) {
        SubmissionDetailsDialog(
            submission = selectedSubmission!!,
            onDismiss = { selectedSubmission = null },
            onDeleteClick = { 
                submissionToDelete = it
                selectedSubmission = null
            }
        )
    }

    if (submissionToDelete != null) {
        AlertDialog(
            onDismissRequest = { submissionToDelete = null },
            title = { Text("Dissolve Writing") },
            text = { Text("This will permanently delete this entry from the Eternal Repository. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        submissionToDelete?.let { onDeleteSubmission(it.id) }
                        submissionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { submissionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    text = "Eternal Repository",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFDAA520),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Saved Writings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(
                onClick = onNavigateBack,
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
        } else if (savedSubmissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your library is empty.", color = Color.Gray)
                    Text("Save entries from the Archive to preserve them.", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                }
            }
        } else {
            Text(
                text = "Stored Entries: ${savedSubmissions.size}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(savedSubmissions) { submission ->
                    SavedSubmissionItem(submission, onClick = { selectedSubmission = submission })
                }
            }
        }
    }
}

@Composable
fun SubmissionDetailsDialog(
    submission: Submissions,
    onDismiss: () -> Unit,
    onDeleteClick: (Submissions) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = submission.gamemode,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatTime(submission.timestamp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    
                    val score = submission.evaluation?.finalScore ?: 0.0
                    Text(
                        text = "%.2f".format(score) + "%",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Divider(color = Color.DarkGray)
                
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = submission.content,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                    
                    if (submission.wordsUsed.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Required Words",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(submission.wordsUsed) { word ->
                                Box(
                                    modifier = Modifier.background(Color(0xFFFFD700).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = word.word, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    submission.evaluation?.feedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "R8 Feedback",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feedback,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDeleteClick(submission) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = borderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Dissolve", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Archive", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun SavedSubmissionItem(submission: Submissions, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onClick
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
                        color = Color(0xFFFFD700)
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
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = submission.content.let { if (it.length > 100) it.take(100) + "..." else it },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "${submission.wordCount} Words", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Box(
                    modifier = Modifier.background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "ETERNAL", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDAA520))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedSubmissionsPreview() {
    Inkr8Theme {
        SavedSubmissionsScreen(savedSubmissions = emptyList(), isLoading = false, onNavigateBack = {}, onDeleteSubmission = {})
    }
}
