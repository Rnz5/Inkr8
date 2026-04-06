package com.inkr8.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.inkr8.data.PlayMode
import com.inkr8.data.StandardWriting
import com.inkr8.data.Submissions
import com.inkr8.data.Tournament
import com.inkr8.data.Words
import com.inkr8.evaluation.SubmissionFactory
import com.inkr8.repository.WordRepository
import com.inkr8.ui.theme.Inkr8Theme
@Composable
fun Writing(
    gamemode: Gamemode,
    playMode: PlayMode,
    tournamentContext: Tournament? = null,
    onAddSubmission: (Submissions) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToResults: () -> Unit
) {

    val wordRepository = remember { WordRepository() }
    var selectedWords by remember { mutableStateOf<List<Words>>(emptyList()) }
    var selectedWordForDialog by remember { mutableStateOf<Words?>(null) }
    var userText by remember { mutableStateOf("") }

    LaunchedEffect(gamemode, playMode, tournamentContext) {
        selectedWords = when {
            playMode is PlayMode.Tournament && tournamentContext != null -> {
                wordRepository.getWordsByTexts(tournamentContext.requiredWords)
            }

            else -> {
                val required = gamemode.requiredWords ?: 0
                if (required > 0) {
                    wordRepository.getRandomWords(required.toLong())
                } else {
                    emptyList()
                }
            }
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

    selectedWordForDialog?.let { word ->
        WordInfoDialog(
            word = word,
            onDismiss = { selectedWordForDialog = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }

        if (selectedWords.isNotEmpty()){
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ){
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(selectedWords) { word ->

                        val isUsed = userText.lowercase().contains(word.word.lowercase())
                        WordButton(
                            word = word,
                            used = isUsed,
                            onClick = { selectedWordForDialog = word }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                    modifier = Modifier.fillMaxWidth().height(220.dp),
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
                            val submission = SubmissionFactory.create(
                                content = userText,
                                gamemode = when (gamemode) {
                                    is StandardWriting -> "STANDARD"
                                    is OnTopicWriting -> "ON_TOPIC"
                                },
                                playMode = when (playMode) {
                                    PlayMode.Practice -> "PRACTICE"
                                    PlayMode.Ranked -> "RANKED"
                                    is PlayMode.Tournament -> "TOURNAMENT"
                                },
                                wordsUsed = selectedWords.filter {
                                    userText.lowercase().contains(it.word.lowercase())
                                },
                                topicId = if (gamemode is OnTopicWriting) gamemode.topic.id else null,
                                themeId = if (gamemode is OnTopicWriting) gamemode.theme.id else null,
                            )
                            onAddSubmission(submission)
                            userText = ""
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
                    Text(
                        if (canSubmit) "Submit"
                        else "Requirements not met"
                    )
                }
            }
        }
    }
}

@Composable
fun WordInfoDialog(
    word: Words,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = word.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Meaning",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = word.definition,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Example sentence",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = word.sentence,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun WordButton(
    word: Words,
    used: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (used)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = word.word,
            fontWeight = if (used) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WritingPreview() {
    Inkr8Theme {
        Writing(
            gamemode = StandardWriting,
            playMode = PlayMode.Practice,
            onAddSubmission = {},
            onNavigateBack = {},
            onNavigateToResults = {}

        )
    }
}