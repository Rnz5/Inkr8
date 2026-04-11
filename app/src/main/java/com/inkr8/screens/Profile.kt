package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.rating.League
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.TimeUtils.formatTime

val fakeUser = Users(
    id = "USR_8492QW",
    name = "MintCake",
    email = "email example",
    merit = 1275,
    rating = 146,
    reputation = 42,
    bestScore = 91.4,
    submissionsCount = 38,
    profileImageURL = "",
    bannerImageURL = "",
    achievements = listOf(),
    joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
    rankedWinStreak = 2,
    rankedLossStreak = 0
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
    onChangeUsername: () -> Unit
) {
    val league = League.fromRating(user.rating)
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeUsernameDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account") },
            text = {
                Column {
                    Text("This will permanently delete your account and release your username.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangeUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showChangeUsernameDialog = false },
            title = { Text("Change username") },
            text = {
                Column {
                    Text("Changing your username will cost ${EconomyConfig.CHANGE_USERNAME} Merit.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Do you want to continue?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChangeUsernameDialog = false
                        onChangeUsername()
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeUsernameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isOwner && user.email == null) {
                    Button(onClick = onLinkGoogle) {
                        Text("Save with Google")
                    }
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                Button(onClick = onNavigateBack) {
                    Text("Back")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.submissionsCount.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Submissions",
                    fontSize = 10.sp
                )
            }

            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (user.email == null) user.name else "${user.name} ✓",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Joined ${formatTime(user.joinedDate)}",
                    fontSize = 10.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.rating.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (pantheonPosition != null) {
                    Text(
                        text = "Pantheon #$pantheonPosition",
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = league.displayName,
                        fontSize = 10.sp
                    )
                }
            }
        }

        InfoCardSection(
            title = "Achievements",
            content = "Work in progress"
        )

        InfoCardSection(
            title = "Best Paragraph",
            content = "Work in progress",
            extraHeight = 80.dp
        )

        if (isOwner) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = onNavigateToSubmissions) {
                    Text("View Submissions")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showChangeUsernameDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Username")
                    }

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log out")
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Account")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCardSection(
    title: String,
    content: String,
    extraHeight: Dp = 20.dp
) {
    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(content)
            Spacer(modifier = Modifier.height(extraHeight))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    Inkr8Theme {
        Profile(
            user = fakeUser,
            pantheonPosition = null,
            isOwner = true,
            onNavigateBack = {},
            onNavigateToSubmissions = {},
            onLinkGoogle = {},
            onLogout = {},
            onDeleteAccount = {},
            onChangeUsername = {}
        )
    }
}