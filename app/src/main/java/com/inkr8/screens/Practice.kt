package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.data.Gamemode
import com.inkr8.data.OnTopicWriting
import com.inkr8.data.StandardWriting
import com.inkr8.data.Theme
import com.inkr8.data.Topic
import com.inkr8.data.Users
import com.inkr8.repository.ThemeRepository
import com.inkr8.repository.TopicRepository
import com.inkr8.repository.WordRepository
import com.inkr8.ui.theme.Inkr8Theme

val fakeUser3 = Users(
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
fun Practice(
    user: Users,
    onNavigateBack: () -> Unit,
    onNavigateToWriting: (Gamemode) -> Unit,
    onNavigateToProfile: () -> Unit
){
    val themeRepository = remember { ThemeRepository() }
    val topicRepository = remember { TopicRepository() }

    var theme by remember { mutableStateOf<Theme?>(null) }
    var topic by remember { mutableStateOf<Topic?>(null) }

    LaunchedEffect(Unit) {

        theme = themeRepository.getRandomTheme()

        theme?.let { selectedTheme -> topic = topicRepository.getRandomTopicFromTheme(selectedTheme.id) }
        println("Theme: $theme")
        println("Topic: $topic")
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = {onNavigateToWriting(StandardWriting)},
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Text(
                        text = "STANDARD - Writing",
                        fontSize = 24.sp,

                        )
                }
                Text(
                    text = "Write a maximum 150 words long paragraph using 4 random words.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 100.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    enabled = theme != null && topic != null,
                    onClick = {
                        theme?.let { t -> topic?.let { tp -> onNavigateToWriting(OnTopicWriting(t, tp)) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Text(
                        text = "ON-TOPIC - Writing",
                        fontSize = 24.sp,

                        )
                }
                Text(
                    text = "Write a maximum 200 words long paragraph about a topic using 2 random words.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }



    Column(
        modifier = Modifier.fillMaxHeight().padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Bottom
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onNavigateBack,
            ) {
                Text("Home")
            }

        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PracticePreview() {
    Inkr8Theme {
        Practice(
            user = fakeUser3,
            onNavigateBack = {},
            onNavigateToWriting = {},
            onNavigateToProfile = {}
        )
    }
}