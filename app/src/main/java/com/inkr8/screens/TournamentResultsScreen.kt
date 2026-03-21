package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentLeaderboardEntry
import com.inkr8.data.TournamentRequirements
import com.inkr8.data.TournamentStatus
import com.inkr8.data.Users
import com.inkr8.ui.theme.Inkr8Theme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TournamentResultsScreen(
    tournament: Tournament,
    leaderboard: List<TournamentLeaderboardEntry>,
    isLoading: Boolean,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onTipUser: (recipientId: String, amount: Long) -> Unit,
    onOpenUserProfile: (String) -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(12.dp)
    ) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = tournament.title.ifBlank { "Tournament Results" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Final Rankings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (leaderboard.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No leaderboard data yet.")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    ResultsHeader()

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = leaderboard,
                            key = { _, item -> item.submission.id }
                        ) { index, entry ->
                            TournamentResultRow(
                                placement = index + 1,
                                entry = entry,
                                currentUserId = currentUserId,
                                onTipUser = onTipUser,
                                onOpenUserProfile = onOpenUserProfile
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Place",
                modifier = Modifier.width(34.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Player",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Score",
                modifier = Modifier.width(56.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Merit",
                modifier = Modifier.width(50.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(46.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
private fun TournamentResultRow(
    placement: Int,
    entry: TournamentLeaderboardEntry,
    currentUserId: String,
    onTipUser: (recipientId: String, amount: Long) -> Unit,
    onOpenUserProfile: (String) -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    val finalScore = entry.submission.evaluation?.finalScore ?: 0.0
    val meritEarned = entry.submission.evaluation?.meritEarned ?: 0L
    val displayName = entry.user?.name?.ifBlank { null } ?: entry.submission.authorId
    val isSelf = entry.submission.authorId == currentUserId

    var showTipDialog by remember { mutableStateOf(false) }

    val cardColor = when (placement) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        3 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    if (showTipDialog) {
        TipAmountDialog(
            recipientName = displayName,
            onDismiss = { showTipDialog = false },
            onSelectAmount = { amount ->
                showTipDialog = false
                onTipUser(entry.submission.authorId, amount)
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPlace(placement),
                modifier = Modifier.width(34.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenUserProfile(entry.submission.authorId) }
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = displayName,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = String.format(Locale.US, "%.2f%%", finalScore),
                modifier = Modifier.width(56.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = formatter.format(meritEarned),
                modifier = Modifier.width(50.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )

            if (!isSelf) {
                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = { showTipDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Tip",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TipAmountDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onSelectAmount: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tip $recipientName") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Button(onClick = { onSelectAmount(100L) }) {
                    Text("100")
                }
                Button(onClick = { onSelectAmount(150L) }) {
                    Text("150")
                }
                Button(onClick = { onSelectAmount(200L) }) {
                    Text("200")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

private fun formatPlace(place: Int): String {
    return when (place) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${place}th"
    }
}

private val previewSubmission1 = Submissions(
    id = "sub1",
    authorId = "user1",
    content = "Preview paragraph",
    evaluation = Evaluation(
        finalScore = 94.25,
        feedback = "Great work",
        meritEarned = 4500,
        rankLeaderboard = 1
    ),
    status = SubmissionStatus.EVALUATED
)

private val previewSubmission2 = Submissions(
    id = "sub2",
    authorId = "user2",
    content = "Preview paragraph",
    evaluation = Evaluation(
        finalScore = 89.75,
        feedback = "Strong writing",
        meritEarned = 2700,
        rankLeaderboard = 2
    ),
    status = SubmissionStatus.EVALUATED
)

private val previewTournament = Tournament(
    id = "t1",
    title = "Standard Writing Tournament - Edition #12",
    creatorId = "host1",
    creatorName = "R8",
    prizePool = 10000,
    maxPlayers = 20,
    minPlayers = 5,
    playersCount = 20,
    status = TournamentStatus.COMPLETED,
    requirements = TournamentRequirements()
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TournamentResultsScreenPreview() {
    Inkr8Theme {
        TournamentResultsScreen(
            tournament = previewTournament,
            leaderboard = listOf(
                TournamentLeaderboardEntry(
                    submission = previewSubmission1,
                    user = Users(id = "user1", name = "MintCake")
                ),
                TournamentLeaderboardEntry(
                    submission = previewSubmission2,
                    user = Users(id = "user2", name = "VelvetAsh")
                )
            ),
            isLoading = false,
            currentUserId = "user1",
            onNavigateBack = {},
            onTipUser = { _, _ -> },
            onOpenUserProfile = {}
        )
    }
}

