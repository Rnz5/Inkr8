package com.inkr8.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Words
import com.inkr8.data.getRandomWordExcluding
import com.inkr8.data.vocabWords
import com.inkr8.ui.theme.Inkr8Theme
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun HomeScreen(
    onNavigateToPractice: () -> Unit,
    onNavigateToCompetitions: () -> Unit,
    onNavigateToProfile: () -> Unit
) {


    var currentWord by remember { mutableStateOf(vocabWords[Random.nextInt(vocabWords.size)]) }
    var showSentence by remember { mutableStateOf(false) }

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
                        text = "Name placeholder",
                        modifier = Modifier.padding(4.dp)
                    )

                    Text(
                        text = "Currency placeholder",
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ){
                    Text(
                        text = "Rank",
                        modifier = Modifier.padding(4.dp)
                    )

                    Text(
                        text = "INT ELO",
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
                    text = "(${currentWord.type}) | rarity",
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
                    onClick = { showSentence = !showSentence },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ){

                    Text(if (showSentence) "Hide Example" else "Show Example")
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
            onNavigateToPractice = {},
            onNavigateToCompetitions = {},
            onNavigateToProfile = {}
        )
    }
}