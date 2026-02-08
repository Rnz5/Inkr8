package com.inkr8.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Gamemode
import com.inkr8.data.OnTopicWriting
import com.inkr8.data.StandardWriting
import com.inkr8.data.Submissions
import com.inkr8.data.Words
import com.inkr8.data.getRandomWords
import com.inkr8.evaluation.SubmissionFactory
import com.inkr8.ui.theme.Inkr8Theme

@Composable
fun WordButton(word: Words, used: Boolean, onClick: () -> Unit) { //i am a freaking genius omg
    Button(
        onClick = onClick,
        colors = if (used) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Text(word.word)
    }
}
@Composable
fun Writing(
    gamemode: Gamemode,
    onAddSubmission: (Submissions) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToResults: () -> Unit
) {

    var userText by remember { mutableStateOf("") }

    val selectedWords = remember(gamemode) {
        val required = gamemode.requiredWords ?: 0
        if (required > 0) {
            getRandomWords(required).take(required)
        } else {
            emptyList()
        }
    }

    val canSubmit = remember(userText, selectedWords, gamemode) {
        if (userText.isBlank()) false
        else {
            val wordCount = userText.split("\\s+".toRegex()).size

            val meetsMinWords = gamemode.minWords?.let { wordCount >= it } ?: true
            val meetsMaxWords = gamemode.maxWords?.let { wordCount <= it } ?: true

            meetsMinWords && meetsMaxWords
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onNavigateBack) {
                Text("X")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedWords.isNotEmpty()){
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ){

                LazyRow(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {

                    items(selectedWords) { word -> val isUsed = userText.lowercase().contains(word.word.lowercase())
                        WordButton(word = word, used = isUsed, onClick = {})
                    }
                }

            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ){

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                if (gamemode is OnTopicWriting) {
                    Text(
                        text = "Theme: ${gamemode.theme.name}"
                    )
                    Text(
                        text = "Topic: ${gamemode.topic.name}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }else{
                    Text(
                        text = "Write something",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = userText,
                    onValueChange = { userText = it },
                    label = { Text("Write a paragraph...") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val wordCount =
                        if (userText.isBlank()){
                            0
                        }else{
                            userText.split("\\s+".toRegex()).size
                        }

                    Text("Words: $wordCount")
                    Text("Characters: ${userText.length}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (canSubmit) {
                            val submission = SubmissionFactory.create(content = userText, gamemode = gamemode, wordsUsed = selectedWords.filter {userText.lowercase().contains(it.word.lowercase())}) //pain
                             onAddSubmission(submission)
                            userText = ""
                            onNavigateToResults()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit,
                    colors = if (canSubmit) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                ) {
                    Text(if (canSubmit){
                        "Submit"
                    } else{
                        "Missing Requirements"
                    }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WritingPreview() {
    Inkr8Theme {
        Writing(
            gamemode = StandardWriting,
            onAddSubmission = {},
            onNavigateBack = {},
            onNavigateToResults = {}
        )
    }
}