package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.*
import com.inkr8.economy.EconomyConfig
import com.inkr8.economy.RankedCostCalculator
import com.inkr8.rating.League
import com.inkr8.repository.FirestoreTournamentRepository
import com.inkr8.repository.ThemeRepository
import com.inkr8.repository.TopicRepository
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.SystemConfig
import com.inkr8.utils.TournamentCard
import com.inkr8.utils.UserHeaderCard

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
    val themeRepository = remember { ThemeRepository() }
    val topicRepository = remember { TopicRepository() }
    
    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var rankedGamemode by remember { mutableStateOf<Gamemode>(StandardWriting) }
    var isGamemodeLoaded by remember { mutableStateOf(false) }

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    DisposableEffect(Unit) {
        val registration = tournamentRepository.listenToTournamentFeed(
            onUpdate = { tournaments = it },
            onError = { it.printStackTrace() }
        )
        onDispose { registration.remove() }
    }

    LaunchedEffect(Unit) {
        if (!isGamemodeLoaded) {
            rankedGamemode = when (league) {
                League.SCRIBE, League.STYLIST -> StandardWriting
                else -> {
                    if ((0..1).random() == 0) {
                        StandardWriting
                    } else {
                        val randomTheme = themeRepository.getRandomTheme()
                        val randomTopic = randomTheme?.let { topicRepository.getRandomTopicFromTheme(it.id) }
                        
                        if (randomTheme != null && randomTopic != null) {
                            OnTopicWriting(randomTheme, randomTopic)
                        } else {
                            StandardWriting
                        }
                    }
                }
            }
            isGamemodeLoaded = true
        }
    }

    val entryCost = RankedCostCalculator.calculateCost(
        EconomyConfig.BASE_COST_RANKED,
        user.rankedWinStreak,
        user.rankedLossStreak,
        user.reputation
    )

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundDark).statusBarsPadding().navigationBarsPadding().padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                UserHeaderCard(
                    user = user,
                    pantheonPosition = pantheonPosition,
                    onClick = onNavigateToProfile
                )
            }

            item {
                Column {
                    Text(
                        text = "Competitive Module",
                        color = primaryGold,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).border(1.dp, Color.Gray, CircleShape).clickable { /* info screen */ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("i", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Ranked Arena",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                IconButton(
                                    onClick = onNavigateToLeaderboard,
                                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape).size(32.dp)
                                ) {
                                    Text("L", color = primaryGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = league.displayName.uppercase(),
                                        color = primaryGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Rating: ${user.rating}",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { if (user.merit >= entryCost) onNavigateToWriting(rankedGamemode) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text("Enter • $entryCost", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryGold.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Host Tournament",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Establish your own directive. Set the stakes. Find the elite.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = onNavigateToCreateTournament,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGold, contentColor = Color.Black)
                        ) {
                            Text("+", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Active Tournaments",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (tournaments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("R8 is cooking something...", color = Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                items(tournaments, key = { it.id }) { tournament ->
                    TournamentCard(
                        tournament = tournament,
                        creatorDisplayName = tournament.creatorName.ifBlank {
                            if (tournament.creatorId == "R8") "R8" else "Unknown"
                        },
                        onClick = { onNavigateToTournamentDetails(tournament) },
                        onHostClick = { onNavigateToUserProfile(tournament.creatorId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Return", fontWeight = FontWeight.Bold)
            }

            Text(
                text = SystemConfig.APP_VERSION,
                color = Color.DarkGray,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompetitionsPreview() {
    val fakeUser = Users(id = "1", name = "MintCake", rating = 850, merit = 5000)
    Inkr8Theme {
        Competitions(
            user = fakeUser,
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
