package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.rating.League
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.TimeUtils.formatTime

private val fakeUser = Users(
    id = "USR_8492QW",
    name = "MintCake",
    email = "email example",
    merit = 1275,
    rating = 146,
    reputation = 42,
    bestScore = 91.43,
    submissionsCount = 38,
    profileImageURL = "",
    bannerImageURL = "",
    achievements = listOf(),
    joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
    rankedWinStreak = 2,
    rankedLossStreak = 0,
    isPhilosopher = true
)

@Composable
fun Profile(
    user: Users,
    isOwner: Boolean,
    pantheonPosition: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToSubmissions: () -> Unit,
    onLinkGoogle: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onChangeUsername: () -> Unit,
    onPurchaseReputation: (onSuccess: () -> Unit) -> Unit
) {
    val league = League.fromRating(user.rating)
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var isReputationRevealed by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Dissolve Identity") },
            text = { Text("This will permanently delete your account and release your username. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDeleteAccount() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showChangeUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showChangeUsernameDialog = false },
            title = { Text("Rebrand Identity") },
            text = { Text("Changing your username will cost ${EconomyConfig.CHANGE_USERNAME} Merit. Continue?") },
            confirmButton = {
                TextButton(onClick = { showChangeUsernameDialog = false; onChangeUsername() }) {
                    Text("Continue")
                }
            },
            dismissButton = { TextButton(onClick = { showChangeUsernameDialog = false }) { Text("Cancel") } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(scrollState).padding(bottom = 32.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)))
            
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Text("Back", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.offset(y = (-50).dp)) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, if(user.isPhilosopher) Color(0xFFFFD700) else Color(0xFFC0C0C0), CircleShape).background(Color.DarkGray)
                )
            }

            Column(
                modifier = Modifier.offset(y = (-40).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                
                if (user.isPhilosopher) {
                    Text(
                        text = "Philosopher",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                
                Text(
                    text = "Member since ${formatTime(user.joinedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-20).dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    "Rating", 
                    user.rating.toString(), 
                    if(pantheonPosition != null) "Pantheon #$pantheonPosition" else league.displayName
                )
                
                VerticalDivider()

                if (isOwner) {
                    StatItem("Merit", user.merit.toString(), "Cap: 50k")
                } else {
                    StatItem("Merit", "Locked", "Private")
                }
            }
        }

        SectionTitle("Battle History")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BattleStat("Submissions", user.submissionsCount.toString())
                BattleStat("Tournaments", user.tournamentsPlayed.toString())
                BattleStat("Victories", user.tournamentsWon.toString())
                BattleStat("Best Score", user.bestScore.toString()+"%")
            }
        }

        SectionTitle("Best Writing")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "\"example text to show off...\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        SectionTitle("Behavioral Standing")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = if (isReputationRevealed) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isReputationRevealed) {
                    Text(text = "Current Reputation", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "${user.reputation}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            user.reputation >= 500 -> Color(0xFF4CAF50)
                            user.reputation <= -500 -> Color(0xFFF44336)
                            else -> Color.White
                        }
                    )
                    Text(text = "Revealed at the cost of ${EconomyConfig.PURCHASE_REPUTATION_VIEW} Merit", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                } else {
                    Text(text = "Reputation Status", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isOwner) {
                        Button(
                            onClick = { 
                                onPurchaseReputation { isReputationRevealed = true }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Reveal Standing - ${EconomyConfig.PURCHASE_REPUTATION_VIEW} Merit")
                        }
                    } else {
                        Text(
                            text = "Hidden",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        if (isOwner) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Button(
                    onClick = onNavigateToSubmissions,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submissions Archive")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showChangeUsernameDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Modify Identity - ${EconomyConfig.CHANGE_USERNAME} Merit")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text("Account Management", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Logout")
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Dissolve")
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.DarkGray)
}

@Composable
fun StatItem(label: String, value: String, subValue: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(text = subValue, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BattleStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        color = Color.Gray
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    Inkr8Theme {
        Profile(
            user = fakeUser,
            pantheonPosition = 4,
            isOwner = true,
            onNavigateBack = {},
            onNavigateToSubmissions = {},
            onLinkGoogle = {},
            onLogout = {},
            onDeleteAccount = {},
            onChangeUsername = {},
            onPurchaseReputation = {}
        )
    }
}