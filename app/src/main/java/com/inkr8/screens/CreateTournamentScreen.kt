package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Users
import com.inkr8.economy.TournamentEconomyCalculator

@Composable
fun CreateTournamentScreen(
    user: Users,
    onCreate: (String, String, Long, Int) -> Unit,
    onBack: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    var gamemode by remember { mutableStateOf("STANDARD") }
    var prizePoolInput by remember { mutableStateOf("") }
    var maxPlayersInput by remember { mutableStateOf("20") }

    val prizePool = prizePoolInput.toLongOrNull() ?: 0L
    val maxPlayers = maxPlayersInput.toIntOrNull() ?: 0

    val projection = remember(prizePool, maxPlayers) {
        if (prizePool > 0 && maxPlayers > 1) {
            TournamentEconomyCalculator.calculateProjection(prizePool, maxPlayers)
        } else null
    }

    val totalCost = projection?.let { it.prizePool + it.systemFee } ?: 0L

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {

        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create Tournament",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tournament Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { gamemode = "STANDARD" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Standard")
                    }

                    Button(
                        onClick = { gamemode = "ON_TOPIC" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("On-Topic")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prizePoolInput,
                        onValueChange = { prizePoolInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Prize Pool") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = maxPlayersInput,
                        onValueChange = { maxPlayersInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Players") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (projection != null) {

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Text(
                                text = "Tournament Economics",
                                fontWeight = FontWeight.Bold
                            )

                            Column {
                                Text(
                                    text = "${projection.entranceFee} Merit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Entry per player", fontSize = 12.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OverviewStatBlock("Prize Pool", "${projection.prizePool}")
                                OverviewStatBlock("System Fee", "${projection.systemFee}")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OverviewStatBlock("Revenue", "${projection.totalRevenue}")
                                OverviewStatBlock("Profit", "${projection.netProfit}")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OverviewStatBlock("Players", "$maxPlayers")
                                OverviewStatBlock("Break-even", "${projection.breakEvenPlayers}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onCreate(title, gamemode, prizePool, maxPlayers)
                    },
                    enabled = title.isNotBlank()
                            && prizePool >= 5000
                            && maxPlayers >= 2
                            && projection != null
                            && user.merit >= totalCost,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    if (projection != null) {
                        Text("Create Tournament • $totalCost Merit")
                    } else {
                        Text("Create Tournament")
                    }
                }

                if (projection != null && user.merit < totalCost) {
                    Text(
                        text = "Not enough Merit",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}