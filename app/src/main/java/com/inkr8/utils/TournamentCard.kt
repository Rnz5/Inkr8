package com.inkr8.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TournamentCard(
    tournament: Tournament,
    creatorDisplayName: String,
    onClick: () -> Unit,
    onHostClick: () -> Unit
) {
    val primaryGold = Color(0xFFFFD700)
    val surfaceDark = Color(0xFF1A1A1A)
    val backgroundDark = Color(0xFF0F0F0F)

    val targetTime = when (tournament.status) {
        TournamentStatus.ENROLLING -> tournament.enrollmentDeadline
        TournamentStatus.ACTIVE -> tournament.submissionDeadline
        else -> 0L
    }

    val remainingText = when (tournament.status) {
        TournamentStatus.ENROLLING -> "ENROLL ENDS IN ${TimeUtils.formatRemainingTime(targetTime)}"
        TournamentStatus.ACTIVE -> "SUBMIT ENDS IN ${TimeUtils.formatRemainingTime(targetTime)}"
        TournamentStatus.EVALUATING -> "R8 IS JUDGING..."
        TournamentStatus.COMPLETED -> "MISSION COMPLETE"
        TournamentStatus.CANCELLED -> "DIRECTIVE ABORTED"
    }

    val formattedPrizePool = NumberFormat.getNumberInstance(Locale.US).format(tournament.prizePool)

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tournament",
                        color = primaryGold.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = tournament.title.ifBlank { "Untitled Arena" }.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Prize Pool",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formattedPrizePool,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryGold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.3f)).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape).background(
                                when (tournament.status) {
                                    TournamentStatus.ENROLLING -> primaryGold
                                    TournamentStatus.ACTIVE -> Color(0xFF4CAF50)
                                    else -> Color.Gray
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tournament.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = remainingText,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onHostClick() }.padding(vertical = 4.dp, horizontal = 0.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pfpexample),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = creatorDisplayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Source Authority",
                            color = Color.DarkGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, primaryGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tournament.gamemode.replace("_", " "),
                        color = primaryGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(
                    text = when (tournament.status) {
                        TournamentStatus.ENROLLING -> "Enter Tournament"
                        TournamentStatus.ACTIVE -> "Submit Entry"
                        else -> "See Results"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
