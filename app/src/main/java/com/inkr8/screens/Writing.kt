package com.inkr8.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.inkr8.data.Words
import com.inkr8.data.getRandomWords
import com.inkr8.ui.theme.Inkr8Theme
import kotlinx.coroutines.selects.select


@Composable
fun WordButton(word: Words, onClick: () -> Unit) { //i am a freaking genius omg
    Button(onClick = onClick) {
        Text(word.word) 
    }
}
@Composable
fun Writing(
    submissions: List<String>,
    onAddSubmission: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var userText by remember { mutableStateOf("") }
    val selectedWords = remember { getRandomWords(4) }
    val requiredWords = selectedWords.map { it.word }


    fun containsAllWords(text: String, requiredWords: List<String>): Boolean {
        val wordsInText = text.lowercase().split("\\W+".toRegex())
        return requiredWords.all { word -> wordsInText.contains(word.lowercase()) }
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ){
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedWords) { word -> WordButton(word = word, onClick = {})
                }
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Write Something",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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


                val canSubmit = userText.isNotBlank() && containsAllWords(userText, requiredWords)
                Button(
                    onClick = {
                        if (canSubmit){
                            onAddSubmission(userText)
                            userText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit
                ) {
                    Text("Submit")
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
            submissions = listOf(),
            onAddSubmission = {},
            onNavigateBack = {},
        )
    }
}