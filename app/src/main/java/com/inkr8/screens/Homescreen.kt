package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

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
        modifier = Modifier.fillMaxSize().background(backgroundDark).statusBarsPadding().navigationBarsPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserHeaderCard(
                user = user,
                pantheonPosition = pantheonPosition,
                onClick = onNavigateToProfile
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "R8 Core",
                    color = primaryGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Active Judgment",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Enhance your writing.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Lexicon Analyzer",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentWord?.word?.uppercase() ?: "...",
                        textAlign = TextAlign.Center,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = currentWord?.pronunciation ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(primaryGold.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentWord?.type?.uppercase() ?: "",
                                color = primaryGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Frequency: ${currentWord?.frequencyScore ?: ""}",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentWord?.definition ?: "",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = Color.LightGray,
                        lineHeight = 22.sp
                    )

                    if (showSentence) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "\"${currentWord?.sentence ?: ""}\"",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                                    Toast.makeText(context, e.message ?: "Access Denied", Toast.LENGTH_SHORT).show()
                                    isSpending = false
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showSentence) surfaceDark else Color.White,
                            contentColor = if (showSentence) Color.Gray else Color.Black,
                            disabledContainerColor = surfaceDark
                        )
                    ) {
                        Text(
                            text = when {
                                showSentence -> "Sentence Shown"
                                isSpending -> "Decrypting..."
                                user.isPhilosopher -> "Reveal usage"
                                else -> "Reveal usage • ${EconomyConfig.SHOW_EXAMPLE_SENTENCE}"
                            },
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToPractice,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = surfaceDark, contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Training", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("Refine Edge", fontSize = 9.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = onNavigateToCompetitions,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGold, contentColor = Color.Black)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Competitions", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("Prove Worth", fontSize = 9.sp, color = Color.Black.copy(alpha = 0.6f))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "pre-alpha v0.4.6",
            fontSize = 9.sp,
            color = Color.DarkGray,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val fakeUser = Users(
        id = "USR_8492QW",
        name = "MintCake",
        merit = 1275,
        rating = 86,
        isPhilosopher = false
    )
    Inkr8Theme {
        HomeScreen(
            user = fakeUser,
            pantheonPosition = 64,
            onNavigateToPractice = {},
            onNavigateToCompetitions = {},
            onNavigateToProfile = {}
        )
    }
}
