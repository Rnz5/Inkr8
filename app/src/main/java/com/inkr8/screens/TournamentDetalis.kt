package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inkr8.R
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentLeaderboardEntry
import com.inkr8.data.TournamentStatus
import com.inkr8.economy.TournamentRewardCalculator
import com.inkr8.utils.TimeUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TournamentDetails(
    tournament: Tournament,
    onNavigateBack: () -> Unit,
    onEnroll: () -> Unit = {},
    onSubmitToTournament: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onHostClick: () -> Unit = {},
    isEnrolled: Boolean = false,
    isSubmitted: Boolean = false,
    isEnrolling: Boolean = false,
    completedLeaderboard: List<TournamentLeaderboardEntry> = emptyList()
){
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val formattedPrizePool = formatter.format(tournament.prizePool)
    val formattedEntryFee = formatter.format(tournament.entranceFee)

    val rewardPercentages =
        TournamentRewardCalculator.calculateRewardPercentages(tournament.maxPlayers.toInt())

    val timeText = when (tournament.status) {
        TournamentStatus.ENROLLING ->
            "Enrollment ends in ${TimeUtils.formatRemainingTime(tournament.enrollmentDeadline)}"
        TournamentStatus.ACTIVE ->
            "Submission ends in ${TimeUtils.formatRemainingTime(tournament.submissionDeadline)}"
        TournamentStatus.EVALUATING ->
            "R8 is evaluating the submissions"
        TournamentStatus.COMPLETED ->
            "Tournament completed"
        TournamentStatus.CANCELLED ->
            "Tournament cancelled"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = tournament.title.ifBlank { "Untitled Tournament" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {

                Text(
                    text = "Reward Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                RewardDistributionHeader()

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (tournament.status == TournamentStatus.COMPLETED && completedLeaderboard.isNotEmpty()) {
                        itemsIndexed(completedLeaderboard) { index, entry ->
                            val merit = entry.submission.evaluation?.meritEarned ?: 0L
                            val scorePercent = if (tournament.prizePool > 0) {
                                (merit.toDouble() / tournament.prizePool.toDouble()) * 100.0
                            } else {
                                0.0
                            }

                            RewardDistributionRow(
                                place = formatPlace(index + 1),
                                merit = formatter.format(merit),
                                percent = "${String.format(Locale.US, "%.2f", scorePercent)}%",
                                participant = entry.user?.name?.ifBlank { null } ?: entry.submission.authorId
                            )
                        }
                    } else {
                        itemsIndexed(rewardPercentages) { index, percent ->
                            val merit = (tournament.prizePool * percent).toLong()

                            RewardDistributionRow(
                                place = formatPlace(index + 1),
                                merit = formatter.format(merit),
                                percent = "${String.format(Locale.US, "%.2f", percent * 100)}%",
                                participant = "TBD"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TournamentOverviewSection(
                    tournament = tournament,
                    formattedPrizePool = formattedPrizePool,
                    formattedEntryFee = formattedEntryFee,
                    timeText = timeText,
                    onHostClick = onHostClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                TournamentRequirementsSection(tournament = tournament)

                Spacer(modifier = Modifier.height(16.dp))

                val actionText = when (tournament.status) {
                    TournamentStatus.ENROLLING -> when {
                        isEnrolled -> "Already Enrolled"
                        isEnrolling -> "Enrolling..."
                        else -> "Enroll - $formattedEntryFee Merit"
                    }

                    TournamentStatus.ACTIVE -> when {
                        !isEnrolled -> "Enrollment Closed"
                        isSubmitted -> "Submission Sent"
                        else -> "Submit to Tournament"
                    }

                    TournamentStatus.EVALUATING -> "R8 is Evaluating"
                    TournamentStatus.COMPLETED -> "View Results"
                    TournamentStatus.CANCELLED -> "Tournament Cancelled"
                }

                val actionEnabled = when (tournament.status) {
                    TournamentStatus.ENROLLING -> !isEnrolled && !isEnrolling
                    TournamentStatus.ACTIVE -> isEnrolled && !isSubmitted
                    TournamentStatus.EVALUATING -> false
                    TournamentStatus.COMPLETED -> true
                    TournamentStatus.CANCELLED -> false
                }

                Button(
                    onClick = {
                        when (tournament.status) {
                            TournamentStatus.ENROLLING -> onEnroll()
                            TournamentStatus.ACTIVE -> onSubmitToTournament()
                            TournamentStatus.COMPLETED -> onViewResults()
                            else -> {}
                        }
                    },
                    enabled = actionEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun RewardDistributionHeader() {
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
            text = "MERIT",
            modifier = Modifier.width(90.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "PERCENT",
            modifier = Modifier.width(90.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "PARTICIPANT",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
}

@Composable
private fun RewardDistributionRow(
    place: String,
    merit: String,
    percent: String,
    participant: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = place,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = merit,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = percent,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = participant,
            modifier = Modifier.weight(1f),
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun TournamentOverviewSection(
    tournament: Tournament,
    formattedPrizePool: String,
    formattedEntryFee: String,
    timeText: String,
    onHostClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onHostClick).padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = "Host profile picture",
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = tournament.creatorName.ifBlank { "Unknown Host" },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tournament.gamemode,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStatBlock(
                    label = "Prize Pool",
                    value = formattedPrizePool
                )
                OverviewStatBlock(
                    label = "Entry",
                    value = "$formattedEntryFee Merit"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStatBlock(
                    label = "Players",
                    value = "${tournament.playersCount}/${tournament.maxPlayers}"
                )
                OverviewStatBlock(
                    label = "Minimum",
                    value = "${tournament.minPlayers}"
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = timeText,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun OverviewStatBlock(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.width(140.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TournamentRequirementsSection(
    tournament: Tournament
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Requirements",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text("Status: ${tournament.status.name}")

            tournament.requirements.minRating?.let {
                Text("Minimum rating: $it")
            }

            tournament.requirements.maxRating?.let {
                Text("Maximum rating: $it")
            }

            tournament.requirements.minReputation?.let {
                Text("Minimum reputation: $it")
            }

            tournament.requirements.minMerit?.let {
                Text("Minimum merit: $it")
            }

            if (
                tournament.requirements.minRating == null &&
                tournament.requirements.maxRating == null &&
                tournament.requirements.minReputation == null &&
                tournament.requirements.minMerit == null
            ) {
                Text(
                    text = "No special restrictions",
                    fontStyle = FontStyle.Italic
                )
            }
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
