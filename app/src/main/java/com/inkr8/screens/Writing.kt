package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.*
import com.inkr8.evaluation.SubmissionFactory
import com.inkr8.repository.WordRepository
import com.inkr8.ui.theme.Inkr8Theme
import kotlinx.coroutines.launch

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
    var selectedThemeForDialog by remember { mutableStateOf<Theme?>(null) }
    var selectedTopicForDialog by remember { mutableStateOf<Topic?>(null) }
    var userText by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

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

    val wordCount = remember(userText) {
        if (userText.isBlank()) 0 else userText.trim().split("\\s+".toRegex()).size
    }

    val canSubmit = remember(userText, selectedWords, gamemode, wordCount) {
        if (userText.isBlank()) false
        else {
            val meetsMinWords = gamemode.minWords?.let { wordCount >= it } ?: true
            val meetsMaxWords = gamemode.maxWords?.let { wordCount <= it } ?: true
            meetsMinWords && meetsMaxWords
        }
    }

    selectedWordForDialog?.let { word ->
        WordInfoDialog(word = word, onDismiss = { selectedWordForDialog = null })
    }

    selectedThemeForDialog?.let { theme ->
        ThemeInfoDialog(theme = theme, onDismiss = { selectedThemeForDialog = null })
    }

    selectedTopicForDialog?.let { topic ->
        TopicInfoDialog(topic = topic, onDismiss = { selectedTopicForDialog = null })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundDark
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding().navigationBarsPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Text("←", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val modeTitle = when(playMode) {
                        is PlayMode.Practice -> "PRACTICE"
                        is PlayMode.Ranked -> "RANKED ARENA"
                        is PlayMode.Tournament -> "TOURNAMENT"
                    }
                    Text(
                        text = modeTitle,
                        color = primaryGold,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "IDENTITY VERIFIED",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (gamemode is OnTopicWriting) {
                DirectiveCard(
                    theme = gamemode.theme,
                    topic = gamemode.topic,
                    onThemeClick = { selectedThemeForDialog = gamemode.theme },
                    onTopicClick = { selectedTopicForDialog = gamemode.topic }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(surfaceDark).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Standard Writing",
                            color = primaryGold,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Construct a superior linguistic entry within standard parameters.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedWords.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Required Words",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedWords) { word ->
                            val isUsed = userText.lowercase().contains(word.word.lowercase())
                            LexiconChip(
                                word = word,
                                isUsed = isUsed,
                                onClick = { selectedWordForDialog = word }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.02f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                TextField(
                    value = userText,
                    onValueChange = { userText = it },
                    placeholder = { 
                        Text(
                            "Start writing...",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyLarge
                        ) 
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = primaryGold,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val min = gamemode.minWords ?: 0
                    val max = gamemode.maxWords ?: 1000
                    val isError = (wordCount < min || wordCount > max) && userText.isNotEmpty()
                    Text(
                        text = "Words: $wordCount",
                        color = if (isError) Color(0xFFF44336) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Constraint: $min - $max",
                        color = Color.DarkGray,
                        fontSize = 10.sp
                    )
                }
                
                Button(
                    onClick = {
                        if (canSubmit) {
                            val qualityCheck = isContentLowQuality(userText)
                            if (qualityCheck.first) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = qualityCheck.second ?: "Low quality submission detected.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

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
                    enabled = canSubmit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit) Color.White else Color.White.copy(alpha = 0.1f),
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.height(48.dp).width(150.dp)
                ) {
                    Text(
                        text = if (canSubmit) "Submit" else "Incomplete",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

private fun isContentLowQuality(content: String): Pair<Boolean, String?> {
    val trimmed = content.trim()
    if (trimmed.length < 50) return true to "Transmission too short (min 50 chars)"

    val words = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }

    if (words.any { it.length > 35 }) {
        return true to "Nonsense detected (excessive word length)"
    }

    if (words.size >= 10) {
        val uniqueWords = words.map { it.lowercase() }.toSet()
        if (uniqueWords.size.toDouble() / words.size.toDouble() < 0.35) {
            return true to "Repetitive content detected"
        }
    }

    val letters = trimmed.replace("[^a-zA-Z]".toRegex(), "")
    if (letters.length > 30) {
        val vowels = letters.count { it.lowercaseChar() in "aeiou" }
        val vowelRatio = vowels.toDouble() / letters.length.toDouble()
        if (vowelRatio < 0.15 || vowelRatio > 0.8) {
            return true to "Unnatural character distribution (nonsense)"
        }

        val uniqueLetters = letters.lowercase().toSet()
        if (uniqueLetters.size < 8 && letters.length > 60) {
            return true to "Low character diversity (nonsense)"
        }
    }

    return false to null
}

@Composable
fun DirectiveCard(
    theme: Theme,
    topic: Topic,
    onThemeClick: () -> Unit,
    onTopicClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF1A1A1A)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Directive",
                color = Color(0xFFFFD700),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onThemeClick() }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Theme", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(theme.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    modifier = Modifier.size(18.dp).border(1.dp, Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("i", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clickable { onTopicClick() }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Topic", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(topic.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(18.dp).border(1.dp, Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("i", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LexiconChip(
    word: Words,
    isUsed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isUsed) Color(0xFFFFD700).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp, 
                if (isUsed) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), 
                RoundedCornerShape(8.dp)
            ).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = word.word,
            color = if (isUsed) Color(0xFFFFD700) else Color.LightGray,
            fontWeight = if (isUsed) FontWeight.Black else FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun WordInfoDialog(word: Words, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Column {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
                Text(text = word.type.lowercase(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Definition", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(word.definition, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text("Example", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("\"${word.sentence}\"", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ThemeInfoDialog(theme: Theme, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                text = theme.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD700)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Directive", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(theme.description, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text("Complexity", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(theme.difficulty, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TopicInfoDialog(topic: Topic, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                text = topic.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Specification", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(topic.description, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text("Complexity", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(topic.difficulty, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
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
