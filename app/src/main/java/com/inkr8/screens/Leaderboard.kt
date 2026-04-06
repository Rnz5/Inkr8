package com.inkr8.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Users
import com.inkr8.rating.League
import com.inkr8.rating.PantheonManager
import com.inkr8.repository.UserRepository
import com.inkr8.ui.theme.Inkr8Theme

@Composable
fun LeaderboardScreen(
    currentUser: Users,
    onNavigateBack: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    var top100 by remember { mutableStateOf<List<Users>>(emptyList()) }
    var leagueCounts by remember { mutableStateOf<Map<League, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        userRepository.getTop100Users { users ->
            top100 = users
        }

        userRepository.getAllUsers { users ->
            leagueCounts = users
                .groupBy { League.fromRating(it.rating) }
                .mapValues { it.value.size }
        }
    }

    LeaderboardContent(
        top100 = top100,
        leagueCounts = leagueCounts,
        currentUser = currentUser,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun LeaderboardContent(
    top100: List<Users>,
    leagueCounts: Map<League, Int>,
    currentUser: Users,
    onNavigateBack: () -> Unit
) {
    val pantheonMembers = top100.filter { it.rating >= PantheonManager.MIN_RATING }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onNavigateBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Text(
                    text = "The Pantheon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (pantheonMembers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "No Pantheon members yet.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            } else {
                itemsIndexed(pantheonMembers) { index, user ->
                    val isCurrentUser = user.id == currentUser.id && user.id != "R8"

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            .border(
                                width = if (isCurrentUser) 2.dp else 0.dp,
                                color = if (isCurrentUser) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1} ${user.name}",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = user.rating.toString(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = "Leagues",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            League.entries.reversed().forEach { league ->
                item {
                    val count = leagueCounts[league] ?: 0

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = league.displayName,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "$count users",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LeaderboardPreview() {
    val fakeTop100 = listOf(
        Users(id = "user1", name = "MintCake", rating = 212),
        Users(id = "user2", name = "VelvetAsh", rating = 205),
        Users(id = "user3", name = "Stony", rating = 198),
        Users(id = "user4", name = "Depression", rating = 191)
    )

    val fakeLeagueCounts = mapOf(
        League.LUMINARY to 96,
        League.LAUREATE to 201,
        League.NOVELIST to 430,
        League.AUTHOR to 812,
        League.STYLIST to 1500,
        League.SCRIBE to 2400
    )

    val fakeCurrentUser = fakeTop100[1]

    Inkr8Theme {
        LeaderboardContent(
            top100 = fakeTop100,
            leagueCounts = fakeLeagueCounts,
            currentUser = fakeCurrentUser,
            onNavigateBack = {}
        )
    }
}