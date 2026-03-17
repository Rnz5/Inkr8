package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
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
){
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
        TournamentStatus.EVALUATING -> "Evaluating"
        TournamentStatus.COMPLETED -> "Completed"
        TournamentStatus.CANCELLED -> "Cancelled"
    }

    val formattedPrizePool = NumberFormat.getNumberInstance(Locale.US).format(tournament.prizePool)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = tournament.title.ifBlank { "Untitled Tournament" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formattedPrizePool,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onHostClick).padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = "host's pfp",
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = creatorDisplayName,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = tournament.gamemode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = remainingText,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Tournament")
            }
        }
    }
}