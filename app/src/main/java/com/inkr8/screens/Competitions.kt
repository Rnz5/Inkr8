package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Gamemode
import com.inkr8.data.OnTopicWriting
import com.inkr8.data.Theme
import com.inkr8.data.Topic
import com.inkr8.data.Tournament
import com.inkr8.data.Users
import com.inkr8.data.StandardWriting
import com.inkr8.economy.EconomyConfig
import com.inkr8.economy.RankedCostCalculator
import com.inkr8.rating.League
import com.inkr8.repository.FirestoreTournamentRepository
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.UserHeaderCard

val fakeUser4 = Users(
    id = "USR_8492QW",
    name = "MintCake",
    email = "email example",
    merit = 1275,
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

@Composable
fun Competitions(
    user: Users,
    pantheonPosition: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToWriting: (Gamemode) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToTournamentDetails: (Tournament) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToCreateTournament: () -> Unit,
) {
    val league = League.fromRating(user.rating)
    val tournamentRepository = remember { FirestoreTournamentRepository() }

    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }

    DisposableEffect(Unit) {
        val registration = tournamentRepository.listenToTournamentFeed(
            onUpdate = { tournaments = it },
            onError = { it.printStackTrace() }
        )

        onDispose {
            registration.remove()
        }
    }

    val rankedGamemode = remember(user.rating) {
        when (league) {
            League.SCRIBE,
            League.STYLIST -> StandardWriting

            else -> {
                if ((0..1).random() == 0) {
                    StandardWriting
                } else {
                    OnTopicWriting(
                        theme = Theme("1", "Creativity"),
                        topic = Topic("1", "The Cost of Silence")
                    )
                }
            }
        }
    }

    val entryCost = RankedCostCalculator.calculateCost(
        EconomyConfig.BASE_COST_RANKED,
        user.rankedWinStreak,
        user.rankedLossStreak,
        user.reputation
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            item {
                UserHeaderCard(
                    user = user,
                    pantheonPosition = pantheonPosition,
                    onClick = onNavigateToProfile
                )
            }

            item {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = {}) {
                                Text("I")
                            }

                            Text(
                                text = "Ranked Mode",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Button(onClick = onNavigateToLeaderboard) {
                                Text("A")
                            }
                        }

                        Text(
                            text = "${league.displayName} | ${user.rating}",
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (user.merit >= entryCost) {
                                    onNavigateToWriting(rankedGamemode)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Play $entryCost Merit")
                        }
                    }
                }
            }

            item {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {

                        Text(
                            text = "Host a Tournament",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Create your own competition. Set the prize, define the rules, and let others compete.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToCreateTournament,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Tournament")
                        }
                    }
                }
            }

            if (tournaments.isEmpty()) {
                item {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active tournaments right now. R8 is probably cooking one.")
                        }
                    }
                }
            } else {
                items(
                    items = tournaments,
                    key = { it.id }
                ) { tournament ->
                    TournamentCard(
                        tournament = tournament,
                        creatorDisplayName = tournament.creatorName.ifBlank { "Unknown Host" },
                        onClick = { onNavigateToTournamentDetails(tournament) },
                        onHostClick = { onNavigateToUserProfile(tournament.creatorId) }
                    )
                }
            }
        }

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.padding(12.dp)
        ) {
            Text("Home")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompetitionsPreview() {
    Inkr8Theme {
        Competitions(
            user = fakeUser4,
            pantheonPosition = null,
            onNavigateBack = {},
            onNavigateToWriting = {},
            onNavigateToProfile = {},
            onNavigateToLeaderboard = {},
            onNavigateToTournamentDetails = {},
            onNavigateToUserProfile = {},
            onNavigateToCreateTournament = {}
        )
    }
}
