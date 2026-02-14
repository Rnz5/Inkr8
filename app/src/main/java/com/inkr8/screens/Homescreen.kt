package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.data.Words
import com.inkr8.data.getRandomWordExcluding
import com.inkr8.data.vocabWords
import com.inkr8.economic.EconomyConfig
import com.inkr8.repository.UserRepository
import com.inkr8.ui.theme.Inkr8Theme
import kotlinx.coroutines.delay
import kotlin.random.Random


val fakeUser2 = Users(
    id = "UASDXAUSIASNI",
    name = "Example User ^^",
    email = null,
    merit = 1000,
    rank = "Unranked",
    elo = 0,
    submissionsCount = 0,
    profileImageURL = "",
    bannerImageURL = "",
    achievements = emptyList(),
    joinedDate = System.currentTimeMillis()
)

@Composable
fun HomeScreen(
    user: Users,
    onNavigateToPractice: () -> Unit,
    onNavigateToCompetitions: () -> Unit,
    onNavigateToProfile: () -> Unit
) {


    var currentWord by remember { mutableStateOf(vocabWords[Random.nextInt(vocabWords.size)]) }
    var showSentence by remember { mutableStateOf(false) }
    val userRepository = UserRepository(FirebaseFirestore.getInstance())
    val context = LocalContext.current
    var isSpending by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        while (true) {
            delay(60000L)
            currentWord = getRandomWordExcluding(currentWord.id)
            showSentence = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ){
        Button(
            onClick = onNavigateToProfile,
            modifier = Modifier.fillMaxWidth()
        ){
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
            ){
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ){
                    Text(
                        text = user.name,
                        modifier = Modifier.padding(4.dp)
                    )

                    Text(
                        text = "Merit: ${user.merit}",
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ){
                    Text(
                        text = user.elo.toString(),
                        modifier = Modifier.padding(4.dp)
                    )

                    Text(
                        text = user.rank,
                        modifier = Modifier.padding(4.dp)
                    )

                }

            }
        }

    }


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
                    text = currentWord.word,
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(4.dp)
                )

                Text(
                    text = currentWord.pronunciation,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "(${currentWord.type}) | ${currentWord.rarity}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = currentWord.definition,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )

                if (showSentence) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Example: ${currentWord.sentence}",
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
            onNavigateToPractice = {},
            onNavigateToCompetitions = {},
            onNavigateToProfile = {}
        )
    }
}