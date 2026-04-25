package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateBack: () -> Unit,
    onUserClick: (Users) -> Unit
) {
    val userRepository = remember { UserRepository() }
    var top100 by remember { mutableStateOf<List<Users>>(emptyList()) }
    var leagueCounts by remember { mutableStateOf<Map<League, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        userRepository.getTop100Users { users ->
            top100 = users
        }

        userRepository.getLeagueCounts { counts ->
            leagueCounts = counts
        }
    }

    LeaderboardContent(
        top100 = top100,
        leagueCounts = leagueCounts,
        currentUser = currentUser,
        onNavigateBack = onNavigateBack,
        onUserClick = onUserClick
    )
}

@Composable
fun LeaderboardContent(
    top100: List<Users>,
    leagueCounts: Map<League, Int>,
    currentUser: Users,
    onNavigateBack: () -> Unit,
    onUserClick: (Users) -> Unit
) {
    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    val pantheonMembers = top100.filter {
        it.rating >= PantheonManager.MIN_RATING && it.id != "R8"
    }

    val r8User = top100.find { it.id == "R8" } ?: Users(id = "R8", name = "R8", rating = 999)

    Box(modifier = Modifier.fillMaxSize().background(backgroundDark)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Text("←", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "System Rankings",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    SectionHeader("The Pantheon", "Elite Standings")
                }

                if (pantheonMembers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = surfaceDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "The Pantheon remains empty.\nRise to claim your seat.",
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    itemsIndexed(pantheonMembers) { index, user ->
                        val isCurrentUser = user.id == currentUser.id
                        PantheonMemberCard(
                            rank = index + 1,
                            user = user,
                            isCurrentUser = isCurrentUser,
                            primaryGold = primaryGold,
                            surfaceDark = surfaceDark,
                            onClick = { onUserClick(user) }
                        )
                    }
                }

                item {
                    Text(
                        text = "The Pantheon updates every 5 minutes.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        letterSpacing = 1.sp
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SectionHeader("Leagues", "System Distribution")
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        League.entries.reversed().forEach { league ->
                            val count = leagueCounts[league] ?: 0
                            LeagueDistributionCard(
                                league = league,
                                count = count,
                                surfaceDark = surfaceDark
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    R8AuthorityCard(r8User, onUserClick)
                }
                
                item {
                    Text(
                        text = "System Authority R8 exists beyond standard metrics.",
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = subtitle,
            color = Color(0xFFFFD700).copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun PantheonMemberCard(
    rank: Int,
    user: Users,
    isCurrentUser: Boolean,
    primaryGold: Color,
    surfaceDark: Color,
    onClick: () -> Unit
) {
    val rankColor = when (rank) {
        1 -> primaryGold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White.copy(alpha = 0.7f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).border(
                width = if (isCurrentUser) 1.dp else 0.dp,
                color = if (isCurrentUser) primaryGold else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                color = rankColor,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.width(48.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (isCurrentUser) {
                    Text(
                        text = "YOU",
                        color = primaryGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = user.rating.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "Rating",
                    color = Color.DarkGray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LeagueDistributionCard(
    league: League,
    count: Int,
    surfaceDark: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceDark.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = league.displayName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tier Level",
                    color = Color.DarkGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Users",
                    color = Color.DarkGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun R8AuthorityCard(r8User: Users, onUserClick: (Users) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onUserClick(r8User) }.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.matchParentSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.02f))
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "System Authority",
                    color = Color(0xFFFFD700).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "R8",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    modifier = Modifier.width(40.dp).clip(CircleShape),
                    thickness = 2.dp,
                    color = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Unrestricted Access",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
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
            onNavigateBack = {},
            onUserClick = {}
        )
    }
}
