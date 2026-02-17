package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Users
import com.inkr8.data.Words
import com.inkr8.economic.EconomyConfig
import com.inkr8.rating.League
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
    var currentWord by remember { mutableStateOf<Words?>(null) }

    var showSentence by remember { mutableStateOf(false) }
    val userRepository = UserRepository(FirebaseFirestore.getInstance())
    val context = LocalContext.current
    var isSpending by remember { mutableStateOf(false) }


    val league = League.fromRating(user.rating)

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

    UserHeaderCard(user = user, pantheonPosition = pantheonPosition, onClick = onNavigateToProfile)


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 340.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentWord?.let { word -> (word.word) }.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )

                Text(
                    text = currentWord?.let { word -> (word.pronunciation) }.toString(),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "(${currentWord?.let { word -> (word.type) }.toString()}) | ${currentWord?.let { word -> (word.frequencyScore) }.toString()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = currentWord?.let { word -> (word.definition) }.toString(),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )

                if (showSentence) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Example: ${currentWord?.let { word -> (word.sentence) }.toString()}",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    enabled = !isSpending && !showSentence,
                    onClick = {
                        isSpending = true
                        userRepository.spendMerit(
                            userId = user.id,
                            amount = EconomyConfig.show_example_sentence,
                            onSuccess = {
                                showSentence = true
                                isSpending = false
                            },
                            onError = {
                                Toast.makeText(context, EconomyConfig.insuffientMerit(), Toast.LENGTH_SHORT).show()
                                isSpending = false
                            }

                        )
                    }
                ){

                    Text(if (showSentence) "Example Shown" else "Show Example ${EconomyConfig.show_example_sentence} Merit")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight().padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Bottom
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(160.dp)
        ) {
            Button(
                onClick = onNavigateToPractice
            ) {
                Text("Practice")
            }

            Button(
                onClick = onNavigateToCompetitions,
            ) {
                Text("Competitions")
            }
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