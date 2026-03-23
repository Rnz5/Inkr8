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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextOverflow
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
                                tournament = tournament,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentResultRow(
    tournament: Tournament,
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

    var showSubmissionSheet by remember { mutableStateOf(false) }
    var showTipDialog by remember { mutableStateOf(false) }

    val cardColor = when (placement) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        3 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    if (showSubmissionSheet) {
        TournamentSubmissionBottomSheet(
            tournament = tournament,
            entry = entry,
            isSelf = isSelf,
            onDismiss = { showSubmissionSheet = false },
            onOpenProfile = {
                showSubmissionSheet = false
                onOpenUserProfile(entry.submission.authorId)
            },
            onTip = { amount -> onTipUser(entry.submission.authorId, amount) }
        )
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
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
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { showSubmissionSheet = true }.padding(horizontal = 2.dp, vertical = 2.dp),
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
                    overflow = TextOverflow.Ellipsis,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentSubmissionBottomSheet(
    tournament: Tournament,
    entry: TournamentLeaderboardEntry,
    isSelf: Boolean,
    onDismiss: () -> Unit,
    onOpenProfile: () -> Unit,
    onTip: (Long) -> Unit
) {
    val submission = entry.submission
    val evaluation = submission.evaluation
    val displayName = entry.user?.name?.ifBlank { null } ?: submission.authorId

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onOpenProfile() }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to open profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = submission.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Submission Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoMiniBlock(
                            label = "Score",
                            value = String.format(Locale.US, "%.2f%%", evaluation?.finalScore ?: 0.0)
                        )
                        InfoMiniBlock(
                            label = "Merit",
                            value = (evaluation?.meritEarned ?: 0L).toString()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoMiniBlock(
                            label = "Characters",
                            value = submission.characterCount.toString()
                        )
                        InfoMiniBlock(
                            label = "Mode",
                            value = submission.gamemode
                        )
                    }

                    if (tournament.gamemode == "ON_TOPIC") {
                        HorizontalDivider()

                        Text(
                            text = "Prompt",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Theme: ${tournament.themeName ?: "Unknown Theme"}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Topic: ${tournament.topicName ?: "Unknown Topic"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (tournament.requiredWords.isNotEmpty()) {
                        HorizontalDivider()

                        Text(
                            text = "Required Words",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        RequiredWordsFlow(
                            requiredWords = tournament.requiredWords,
                            content = submission.content
                        )
                    }
                }
            }

            if (!isSelf) {
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Send Tip",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100L, 150L, 200L).forEach { amount ->
                                Button(
                                    onClick = { onTip(amount) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(amount.toString())
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoMiniBlock(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.width(120.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RequiredWordsFlow(
    requiredWords: List<String>,
    content: String
) {
    val normalizedContentWords = content.lowercase().split("\\W+".toRegex()).filter { it.isNotBlank() }.toSet()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        requiredWords.chunked(2).forEach { rowWords ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowWords.forEach { word ->
                    val used = normalizedContentWords.contains(word.lowercase())

                    FilterChip(
                        selected = used,
                        onClick = {},
                        label = {
                            Text(
                                text = if (used) "$word ✓" else word
                            )
                        },
                        enabled = false
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

