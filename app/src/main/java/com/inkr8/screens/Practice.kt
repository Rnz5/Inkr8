package com.inkr8.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inkr8.data.*
import com.inkr8.repository.ThemeRepository
import com.inkr8.repository.TopicRepository
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.UserHeaderCard

@Composable
fun Practice(
    user: Users,
    pantheonPosition: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToWriting: (Gamemode) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val themeRepository = remember { ThemeRepository() }
    val topicRepository = remember { TopicRepository() }

    var theme by remember { mutableStateOf<Theme?>(null) }
    var topic by remember { mutableStateOf<Topic?>(null) }

    LaunchedEffect(Unit) {
        theme = themeRepository.getRandomTheme()
        theme?.let { topic = topicRepository.getRandomTopicFromTheme(it.id) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            UserHeaderCard(
                user = user,
                pantheonPosition = pantheonPosition,
                onClick = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Practice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Refine your writing without risk.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "STANDARD",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Free writing using 4 required words.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigateToWriting(StandardWriting) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "ON-TOPIC",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Write about a specific theme and topic using 2 required words.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        enabled = theme != null && topic != null,
                        onClick = {
                            theme?.let { t ->
                                topic?.let { tp ->
                                    onNavigateToWriting(OnTopicWriting(t, tp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start")
                    }
                }
            }
        }

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Home")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PracticePreview() {

    val fakeUser = Users(
        id = "USR_8492QW",
        name = "MintCake",
        email = "example@email.com",
        merit = 1275,
        rating = 86,
        reputation = 42,
        bestScore = 91.4,
        submissionsCount = 38,
        profileImageURL = "",
        bannerImageURL = "",
        achievements = listOf(),
        joinedDate = System.currentTimeMillis(),
        rankedWinStreak = 2,
        rankedLossStreak = 0
    )

    Inkr8Theme {
        Practice(
            user = fakeUser,
            pantheonPosition = null,
            onNavigateBack = {},
            onNavigateToWriting = {},
            onNavigateToProfile = {}
        )
    }
}