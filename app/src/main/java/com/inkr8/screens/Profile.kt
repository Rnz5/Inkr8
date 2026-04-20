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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.rating.League
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.TimeUtils.formatTime
import java.text.NumberFormat
import java.util.Locale

@Composable
fun Profile(
    user: Users,
    isOwner: Boolean,
    pantheonPosition: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToSubmissions: () -> Unit,
    onNavigateToSavedSubmissions: () -> Unit,
    onLinkGoogle: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onChangeUsername: () -> Unit,
    onPurchaseReputation: (onSuccess: () -> Unit) -> Unit,
    onExpandCap: () -> Unit
) {
    val league = League.fromRating(user.rating)
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showExpandCapDialog by remember { mutableStateOf(false) }
    var isReputationRevealed by remember { mutableStateOf(false) }

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = surfaceDark,
            title = { Text("Dissolve Identity", color = Color.White) },
            text = { Text("This will permanently delete your account and release your username. This action cannot be undone.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDeleteAccount() }) {
                    Text("Dissolve", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = Color.White) } }
        )
    }

    if (showChangeUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showChangeUsernameDialog = false },
            containerColor = surfaceDark,
            title = { Text("Rebrand Identity", color = Color.White) },
            text = { Text("Changing your username will cost ${EconomyConfig.CHANGE_USERNAME} Merit. Continue?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showChangeUsernameDialog = false; onChangeUsername() }) {
                    Text("Continue", color = primaryGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showChangeUsernameDialog = false }) { Text("Cancel", color = Color.White) } }
        )
    }

    if (showExpandCapDialog) {
        val expandCost = (user.meritCap * 0.25).toLong()
        AlertDialog(
            onDismissRequest = { showExpandCapDialog = false },
            containerColor = surfaceDark,
            title = { Text("Expand Merit Cap", color = primaryGold, fontWeight = FontWeight.Black) },
            text = { 
                Column {
                    Text("Current Cap: ${NumberFormat.getNumberInstance(Locale.US).format(user.meritCap)}", color = Color.White)
                    Text("Expansion Cost: ${NumberFormat.getNumberInstance(Locale.US).format(expandCost)} Merit", color = primaryGold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This will increase your liquid capacity by 10,000 merit. Extra earnings are currently stored in SRR (Hold).", color = Color.Gray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExpandCapDialog = false; onExpandCap() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Expand", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = { TextButton(onClick = { showExpandCapDialog = false }) { Text("Cancel", color = Color.White) } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundDark).verticalScroll(scrollState)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)))
            // banner here
            
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Text("Back", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.offset(y = (-50).dp)) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, if(user.isPhilosopher) primaryGold else Color(0xFFC0C0C0), CircleShape).background(surfaceDark),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.offset(y = (-40).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                
                if (user.isPhilosopher) {
                    Text(
                        text = "Philosopher",
                        color = primaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }
                
                Text(
                    text = "Member since ${formatTime(user.joinedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    letterSpacing = 1.sp
                )
            }
        }

        if (isOwner) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-20).dp),
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
                        Column {
                            Text("Liquid Merit", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(
                                text = NumberFormat.getNumberInstance(Locale.US).format(user.merit),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Button(
                            onClick = { showExpandCapDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Expand Cap", fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    val progress = (user.merit.toFloat() / user.meritCap.toFloat()).coerceIn(0f, 1f)
                    Column {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = primaryGold,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Capacity: ${NumberFormat.getNumberInstance(Locale.US).format(user.meritCap)}", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${(progress * 100).toInt()}%", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (user.meritHold > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.03f)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SRR (HOLD)", color = primaryGold.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(
                                    text = NumberFormat.getNumberInstance(Locale.US).format(user.meritHold),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Slow Release Active",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = if (isOwner) 8.dp else 0.dp).offset(y = if (isOwner) 0.dp else (-20).dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem("Rating", user.rating.toString(), if(pantheonPosition != null) "PANTHEON #$pantheonPosition" else league.displayName.uppercase())
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.05f)))
                StatItem("Reputation", if(isReputationRevealed || !isOwner) user.reputation.toString() else "LOCKED", "BEHAVIORAL")
            }
        }

        SectionTitle("Battle History")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BattleStatSmall(Modifier.weight(1f), "Submissions", user.submissionsCount.toString())
            BattleStatSmall(Modifier.weight(1f), "Tournaments", user.tournamentsPlayed.toString())
            BattleStatSmall(Modifier.weight(1f), "Victories", user.tournamentsWon.toString())
            BattleStatSmall(Modifier.weight(1f), "Best", "${user.bestScore}%")
        }

        SectionTitle("System Archive")
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileActionButton(
                title = "Archive Entries",
                subtitle = "Review and refine your history",
                onClick = onNavigateToSubmissions,
                containerColor = Color.White,
                contentColor = Color.Black
            )
            
            ProfileActionButton(
                title = "Eternal Repository",
                subtitle = "Locked and protected manuscripts",
                onClick = onNavigateToSavedSubmissions,
                containerColor = surfaceDark,
                contentColor = primaryGold,
                showBorder = true
            )
        }

        if (isOwner) {
            SectionTitle("Behavioral Protocols")
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (isReputationRevealed) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = surfaceDark)
                    ) {
                        Text(
                            text = "Reputation is fully integrated with system standing. Low standing increases entry fees and limits access to tournaments.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Button(
                        onClick = { onPurchaseReputation { isReputationRevealed = true } },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text("Reveal Reputation • ${EconomyConfig.PURCHASE_REPUTATION_VIEW} Merit", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }

            SectionTitle("Management")
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showChangeUsernameDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Modify Identity", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Logout")
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.6f))
                    ) {
                        Text("Dissolve")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ProfileActionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    showBorder: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        border = if (showBorder) androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.2f)) else null,
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                Text(text = subtitle.uppercase(), fontSize = 9.sp, color = contentColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            }
            Text("→", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, subValue: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(text = subValue, fontSize = 9.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun BattleStatSmall(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label, fontSize = 8.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp),
        color = Color.DarkGray
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    Inkr8Theme {
        Profile(
            user = Users(name = "MintCake", merit = 45000, meritCap = 50000, meritHold = 1250, isPhilosopher = true),
            pantheonPosition = 4,
            isOwner = true,
            onNavigateBack = {},
            onNavigateToSubmissions = {},
            onNavigateToSavedSubmissions = {},
            onLinkGoogle = {},
            onLogout = {},
            onDeleteAccount = {},
            onChangeUsername = {},
            onPurchaseReputation = {},
            onExpandCap = {}
        )
    }
}
