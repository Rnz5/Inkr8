package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inkr8.data.Users
import com.inkr8.rating.League
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
            leagueCounts = users.groupBy { League.fromRating(it.rating) }.mapValues { it.value.size }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {

        Text(
            text = "THE PANTHEON",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(top100) { index, user ->

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "#${index + 1} ${user.name}")
                    Text(text = "${user.rating}")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            League.entries.reversed().forEach { league ->

                item {
                    val count = leagueCounts[league] ?: 0
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(text = league.displayName)
                            Text(text = "Users: $count")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardPreview() {

    val fakeTop100 = List(10) {
        Users(
            id = "user$it",
            name = "Writer$it",
            rating = 200 - it.toLong(),
        )
    }

    val fakeLeagueCounts = mapOf(
        League.LUMINARY to 96,
        League.LAUREATE to 201,
        League.NOVELIST to 430,
        League.AUTHOR to 812,
        League.STYLIST to 1500,
        League.SCRIBE to 2400
    )

    val fakeCurrentUser = fakeTop100[3]
    Inkr8Theme {
        LeaderboardContent(
            top100 = fakeTop100,
            leagueCounts = fakeLeagueCounts,
            currentUser = fakeCurrentUser,
            onNavigateBack = {}
        )
    }
}

@Composable
fun LeaderboardContent(
    top100: List<Users>,
    leagueCounts: Map<League, Int>,
    currentUser: Users,
    onNavigateBack: () -> Unit
) {
}
