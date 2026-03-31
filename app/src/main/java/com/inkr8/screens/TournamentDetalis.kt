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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.scheduler.Requirements
import com.inkr8.R
import com.inkr8.data.Evaluation
import com.inkr8.data.Submissions
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentLeaderboardEntry
import com.inkr8.data.TournamentRequirements
import com.inkr8.data.TournamentStatus
import com.inkr8.data.Users
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
    onOpenSubmission: (Submissions) -> Unit = {},
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
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = tournament.title.ifBlank { "Untitled Tournament" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp)
            ) {

                Text(
                    text = "Reward Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                RewardDistributionHeader(showScoreInsteadOfPercent = tournament.status == TournamentStatus.COMPLETED && completedLeaderboard.isNotEmpty())

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    if (tournament.status == TournamentStatus.COMPLETED && completedLeaderboard.isNotEmpty()) {

                        itemsIndexed(completedLeaderboard) { index, entry ->

                            val merit = entry.submission.evaluation?.meritEarned ?: 0L

                            val score = entry.submission.evaluation?.finalScore ?: 0.0

                            RewardDistributionRow(
                                place = formatPlace(index + 1),
                                merit = formatter.format(merit),
                                percent = String.format(Locale.US, "%.2f", score),
                                participant = entry.user?.name?.ifBlank { null } ?: entry.submission.authorId,
                                onClick = { onOpenSubmission(entry.submission) }
                            )
                        }

                    } else {

                        itemsIndexed(rewardPercentages) { index, percent ->

                            val merit = (tournament.prizePool * percent).toLong()

                            RewardDistributionRow(
                                place = formatPlace(index + 1),
                                merit = formatter.format(merit),
                                percent = "${String.format(Locale.US, "%.2f", percent * 100)}%",
                                participant = "TBD",
                                onClick = {}
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TournamentOverviewSection(
                    tournament = tournament,
                    formattedPrizePool = formattedPrizePool,
                    formattedEntryFee = formattedEntryFee,
                    timeText = timeText,
                    onHostClick = onHostClick
                )

                Spacer(modifier = Modifier.height(6.dp))

                TournamentRequirementsSection(tournament = tournament)

                Spacer(modifier = Modifier.height(6.dp))

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
private fun RewardDistributionHeader(
    showScoreInsteadOfPercent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PLACE",
            modifier = Modifier.width(52.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = "MERIT",
            modifier = Modifier.width(88.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = if (showScoreInsteadOfPercent) "SCORE" else "PERCENT",
            modifier = Modifier.width(72.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = "PARTICIPANT",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider()
}
@Composable
private fun RewardDistributionRow(
    place: String,
    merit: String,
    percent: String,
    participant: String,
    onClick: () -> Unit
) {
    val placeColor = when (place) {
        "1st" -> Color(0xFFFFD700)
        "2nd" -> Color.LightGray
        "3rd" -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = place,
            modifier = Modifier.width(52.dp),
            color = placeColor,
            fontWeight = if (place == "1st" || place == "2nd" || place == "3rd") {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )

        Text(
            text = merit,
            modifier = Modifier.width(88.dp)
        )

        Text(
            text = percent,
            modifier = Modifier.width(72.dp)
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

@Composable
private fun previewTournament(): Tournament {
    return Tournament(
        id = "t1",
        title = "Precision Writing Arena",
        creatorId = "R8",
        creatorName = "R8",
        prizePool = 10000,
        entranceFee = 500,
        playersCount = 12,
        maxPlayers = 20,
        minPlayers = 5,
        gamemode = "ON_TOPIC",
        status = TournamentStatus.ENROLLING,
        enrollmentDeadline = System.currentTimeMillis() + 1000000,
        submissionDeadline = System.currentTimeMillis() + 2000000,
        requirements = TournamentRequirements()
    )
}

@Composable
private fun previewLeaderboard(): List<TournamentLeaderboardEntry> {
    return listOf(
        TournamentLeaderboardEntry(
            submission = Submissions(
                id = "s1",
                authorId = "user1",
                content = "Sample",
                evaluation = Evaluation(
                    finalScore = 91.23,
                    meritEarned = 5000
                )
            ),
            user = Users(
                id = "USR_8492QW",
                name = "MintCake",
                email = "email example",
                merit = 1275,
                rating = 146,
                reputation = 42,
                bestScore = 91.4,
                submissionsCount = 38,
                profileImageURL = "",
                bannerImageURL = "",
                achievements = listOf(),
                joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
                rankedWinStreak = 2,
                rankedLossStreak = 0
            )
        ),
        TournamentLeaderboardEntry(
            submission = Submissions(
                id = "s2",
                authorId = "user2",
                content = "Sample",
                evaluation = Evaluation(
                    finalScore = 87.12,
                    meritEarned = 3000
                )
            ),
            user = Users(
                id = "USR_8492QW",
                name = "Shrimpy",
                email = "email example",
                merit = 1275,
                rating = 146,
                reputation = 42,
                bestScore = 91.4,
                submissionsCount = 38,
                profileImageURL = "",
                bannerImageURL = "",
                achievements = listOf(),
                joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
                rankedWinStreak = 2,
                rankedLossStreak = 0
            )
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TournamentDetailsPreview() {

    TournamentDetails(
        tournament = previewTournament(),
        onNavigateBack = {},
        onEnroll = {},
        onSubmitToTournament = {},
        onViewResults = {},
        onHostClick = {},
        onOpenSubmission = {},
        isEnrolled = false,
        isSubmitted = false,
        isEnrolling = false,
        completedLeaderboard = previewLeaderboard() //emptyList()
    )
}