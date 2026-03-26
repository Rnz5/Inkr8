package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig.insufficientMerit
import com.inkr8.economy.TournamentEconomyCalculator
import com.inkr8.ui.theme.Inkr8Theme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreateTournamentScreen(
    user: Users,
    onCreate: (String, String, Long, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    var title by remember { mutableStateOf("") }
    var gamemode by remember { mutableStateOf("STANDARD") }
    var prizePoolInput by remember { mutableStateOf("") }
    var maxPlayersInput by remember { mutableStateOf("20") }

    val prizePool = prizePoolInput.toLongOrNull() ?: 0L
    val maxPlayers = maxPlayersInput.toIntOrNull() ?: 0

    val projection = remember(prizePool, maxPlayers) {
        if (maxPlayers > 1) {
            TournamentEconomyCalculator.calculateProjection(
                prizePool = prizePool.coerceAtLeast(1L),
                maxPlayers = maxPlayers
            )
        } else {
            null
        }
    }

    val displayedPrizePool = if (prizePool > 0) prizePool else 0L
    val displayedEntryFee = projection?.entranceFee ?: 0L
    val displayedSystemFee = if (prizePool > 0) projection?.systemFee ?: 0L else 0L
    val displayedRevenue = projection?.totalRevenue ?: 0L
    val displayedProfit = if (prizePool > 0) projection?.netProfit ?: 0L else 0L
    val displayedBreakEven = projection?.breakEvenPlayers ?: 0
    val totalCost = displayedPrizePool + displayedSystemFee

    val canCreate =
        title.isNotBlank() &&
                prizePool >= 5000L &&
                maxPlayers >= 2 &&
                user.merit >= totalCost

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create Tournament",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Set the stakes, define the mode, and let others fight for your prize pool.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tournament Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gamemode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GamemodeSelectorChip(
                            text = "Standard",
                            selected = gamemode == "STANDARD",
                            onClick = { gamemode = "STANDARD" },
                            modifier = Modifier.weight(1f)
                        )

                        GamemodeSelectorChip(
                            text = "On-Topic",
                            selected = gamemode == "ON_TOPIC",
                            onClick = { gamemode = "ON_TOPIC" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prizePoolInput,
                        onValueChange = { prizePoolInput = it.filter(Char::isDigit) },
                        label = { Text("Prize Pool") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = maxPlayersInput,
                        onValueChange = { maxPlayersInput = it.filter(Char::isDigit) },
                        label = { Text("Players") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Tournament Economics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Column {
                            Text(
                                text = "${formatter.format(displayedEntryFee)} Merit",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Entry per player",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EconomyStatBlock(
                                label = "Prize Pool",
                                value = formatter.format(displayedPrizePool)
                            )
                            EconomyStatBlock(
                                label = "System Fee",
                                value = formatter.format(displayedSystemFee)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EconomyStatBlock(
                                label = "Revenue",
                                value = formatter.format(displayedRevenue)
                            )
                            EconomyStatBlock(
                                label = "Profit",
                                value = formatter.format(displayedProfit)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EconomyStatBlock(
                                label = "Players",
                                value = if (maxPlayers > 0) maxPlayers.toString() else "0"
                            )
                            EconomyStatBlock(
                                label = "Break-even",
                                value = displayedBreakEven.toString()
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Cost",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Prize Pool + System Fee",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Text(
                                    text = "${formatter.format(totalCost)} Merit",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        when {
                            title.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Tournament title is required",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            prizePool < 5000L -> {
                                Toast.makeText(
                                    context,
                                    "The minimum prize pool is 5,000 Merit",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            maxPlayers < 2 -> {
                                Toast.makeText(
                                    context,
                                    "At least 2 players are required",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            user.merit < totalCost -> {
                                Toast.makeText(
                                    context,
                                    insufficientMerit(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                onCreate(title, gamemode, prizePool, maxPlayers)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Tournament • ${formatter.format(totalCost)} Merit")
                }
            }
        }
    }
}

@Composable
private fun GamemodeSelectorChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(containerColor)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp)
            ).clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun EconomyStatBlock(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.width(130.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private val previewCreateTournamentUser = Users(
    id = "USR_8492QW",
    name = "MintCake",
    email = "email example",
    merit = 18000,
    rating = 86,
    reputation = 42,
    bestScore = 91.4,
    submissionsCount = 38,
    profileImageURL = "",
    bannerImageURL = "",
    achievements = listOf(),
    joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
    rankedWinStreak = 3,
    rankedLossStreak = 0
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateTournamentScreenPreview() {
    Inkr8Theme {
        CreateTournamentScreen(
            user = previewCreateTournamentUser,
            onCreate = { _, _, _, _ -> },
            onBack = {}
        )
    }
}