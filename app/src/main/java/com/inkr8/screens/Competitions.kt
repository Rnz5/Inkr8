package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.inkr8.data.getRandomThemeAndTopic
import com.inkr8.data.StandardWriting
import com.inkr8.data.Users
import com.inkr8.ui.theme.Inkr8Theme

val fakeUser4 = Users(
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
fun Competitions(
    user: Users,
    onNavigateBack: () -> Unit,
    onNavigateToWriting: (Gamemode) -> Unit,
    onNavigateToProfile: () -> Unit
) {

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

    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(800.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

    }



    Column(
        modifier = Modifier.fillMaxHeight().padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Bottom
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
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
fun CompetitionsPreview() {
    Inkr8Theme {
        Competitions(
            user = fakeUser4,
            onNavigateBack = {},
            onNavigateToWriting = {},
            onNavigateToProfile = {}
        )
    }
}