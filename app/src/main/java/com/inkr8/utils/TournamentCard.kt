package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentStatus
import com.inkr8.utils.TimeUtils
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TournamentCard(
    tournament: Tournament,
    creatorDisplayName: String,
    onClick: () -> Unit,
    onHostClick: () -> Unit
) {

    val targetTime = when (tournament.status) {
        TournamentStatus.ENROLLING -> tournament.enrollmentDeadline
        TournamentStatus.ACTIVE -> tournament.submissionDeadline
        else -> 0L
    }

    var ticker by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(targetTime) {
        while (true) {
            ticker = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val remainingText = when (tournament.status) {
        TournamentStatus.ENROLLING -> "Enroll ends in ${TimeUtils.formatRemainingTime(targetTime)}"
        TournamentStatus.ACTIVE -> "Submit ends in ${TimeUtils.formatRemainingTime(targetTime)}"
        TournamentStatus.EVALUATING -> "R8 is judging..."
        TournamentStatus.COMPLETED -> "Completed"
        TournamentStatus.CANCELLED -> "Cancelled"
    }

    val formattedPrizePool = NumberFormat.getNumberInstance(Locale.US).format(tournament.prizePool)

    val isR8 = tournament.creatorId == "R8"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = tournament.title.ifBlank { "Untitled Tournament" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formattedPrizePool,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = tournament.status.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (tournament.status) {
                        TournamentStatus.ENROLLING -> MaterialTheme.colorScheme.primary
                        TournamentStatus.ACTIVE -> MaterialTheme.colorScheme.secondary
                        else -> Color.Gray
                    }
                )

                Text(
                    text = remainingText,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onHostClick).padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Text(
                            text = creatorDisplayName,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = tournament.gamemode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (tournament.status) {
                        TournamentStatus.ENROLLING -> "Enter"
                        TournamentStatus.ACTIVE -> "Submit"
                        else -> "View"
                    }
                )
            }
        }
    }
}