package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Users
import com.inkr8.data.Words
import com.inkr8.economy.EconomyConfig
import com.inkr8.repository.UserRepository
import com.inkr8.repository.WordRepository
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.UserHeaderCard
import kotlinx.coroutines.delay

val fakeUser2 = Users(
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
    rankedWinStreak = 2,
    rankedLossStreak = 0
)

@Composable
fun HomeScreen(
    user: Users,
    pantheonPosition: Int?,
    onNavigateToPractice: () -> Unit,
    onNavigateToCompetitions: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val wordRepository = remember { WordRepository() }
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current

    var currentWord by remember { mutableStateOf<Words?>(null) }
    var showSentence by remember { mutableStateOf(false) }
    var isSpending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        suspend fun loadNewWord() {
            currentWord = wordRepository.getSingleRandomWord()
            showSentence = false
        }

        loadNewWord()

        while (true) {
            delay(60000L)
            loadNewWord()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            UserHeaderCard(
                user = user,
                pantheonPosition = pantheonPosition,
                onClick = onNavigateToProfile
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "R8",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "The system is watching.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentWord?.word ?: "...",
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentWord?.pronunciation ?: "",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "(${currentWord?.type ?: ""}) | ${currentWord?.frequencyScore ?: ""}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentWord?.definition ?: "",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    if (showSentence) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Example: ${currentWord?.sentence ?: ""}",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        enabled = !isSpending && !showSentence,
                        onClick = {
                            if (user.isPhilosopher) {
                                showSentence = true
                                return@Button
                            }

                            isSpending = true
                            userRepository.applyMeritAction(
                                action = "PURCHASE_EXAMPLE_SENTENCE",
                                onSuccess = {
                                    showSentence = true
                                    isSpending = false
                                },
                                onError = { e ->
                                    Toast.makeText(
                                        context,
                                        e.message ?: "Failed to purchase example sentence",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isSpending = false
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                showSentence -> "Example Shown"
                                isSpending -> "Unlocking..."
                                user.isPhilosopher -> "Show Example — Free"
                                else -> "Show Example — ${EconomyConfig.SHOW_EXAMPLE_SENTENCE} Merit"
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToPractice,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Practice")
                }

                Button(
                    onClick = onNavigateToCompetitions,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Competitions")
                }
            }

            Text(
                text = "pre-alpha v0.4.5",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Inkr8Theme {
        HomeScreen(
            user = fakeUser2,
            pantheonPosition = 64,
            onNavigateToPractice = {},
            onNavigateToCompetitions = {},
            onNavigateToProfile = {}
        )
    }
}