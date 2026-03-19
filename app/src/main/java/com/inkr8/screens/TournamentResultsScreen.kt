package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inkr8.R
import com.inkr8.data.Submissions
import com.inkr8.data.Tournament
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TournamentResultsScreen(
    tournament: Tournament,
    leaderboard: List<Submissions>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
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
                            key = { _, item -> item.id }
                        ) { index, submission ->
                            TournamentResultRow(
                                placement = index + 1,
                                submission = submission
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
                text = "PLACE",
                modifier = Modifier.width(60.dp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "PLAYER",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SCORE",
                modifier = Modifier.width(80.dp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "MERIT",
                modifier = Modifier.width(90.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
private fun TournamentResultRow(
    placement: Int,
    submission: Submissions
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    val finalScore = submission.evaluation?.finalScore ?: 0.0
    val meritEarned = submission.evaluation?.meritEarned ?: 0L

    val cardColor = when (placement) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        3 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPlace(placement),
                modifier = Modifier.width(60.dp),
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = submission.authorId,
                    maxLines = 1
                )
            }

            Text(
                text = String.format(Locale.US, "%.2f%%", finalScore),
                modifier = Modifier.width(80.dp),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = formatter.format(meritEarned),
                modifier = Modifier.width(90.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatPlace(place: Int): String {
    return when (place) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${place}th"
    }
}

