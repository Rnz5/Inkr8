package com.inkr8.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundDark).statusBarsPadding().navigationBarsPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        UserHeaderCard(
            user = user,
            pantheonPosition = pantheonPosition,
            onClick = onNavigateToProfile
        )

        Column {
            Text(
                text = "Training Modules",
                color = primaryGold,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Refine your linguistic edge.",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "No risk to ranking. R8 is still judging.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp
            )
        }

        PracticeModuleCard(
            moduleNumber = "01",
            title = "Standard Writing",
            description = "Unconstrained composition using 4 random words entries.",
            constraints = "Max 150 words • 4 words",
            onClick = { onNavigateToWriting(StandardWriting) },
            enabled = true,
            primaryGold = primaryGold
        )

        PracticeModuleCard(
            moduleNumber = "02",
            title = "On-Topic Writing",
            description = "Specific parameters. Requires Theme and Topic adherence using 2 random words entries.",
            constraints = "Max 200 words • Theme + Topic",
            onClick = {
                theme?.let { t ->
                    topic?.let { tp ->
                        onNavigateToWriting(OnTopicWriting(t, tp))
                    }
                }
            },
            enabled = theme != null && topic != null,
            primaryGold = primaryGold
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "pre-alpha v0.4.6",
                color = Color.DarkGray,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
            
            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Return", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PracticeModuleCard(
    moduleNumber: String,
    title: String,
    description: String,
    constraints: String,
    onClick: () -> Unit,
    enabled: Boolean,
    primaryGold: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, if (enabled) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (enabled) Color.White else Color.Gray,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = moduleNumber,
                    color = primaryGold.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = if (enabled) Color.LightGray else Color.DarkGray,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = constraints,
                    color = if (enabled) primaryGold.copy(alpha = 0.7f) else Color.DarkGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(if (enabled) Color.White else Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "→",
                        color = if (enabled) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
