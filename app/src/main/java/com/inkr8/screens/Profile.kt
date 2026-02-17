package com.inkr8.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.AuthManager
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.rating.League
import com.inkr8.rating.PantheonManager
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.TimeUtils.formatTime

val fakeUser = Users(
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
fun Profile(
    user: Users,
    isOwner: Boolean,
    pantheonPosition: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToSubmissions: () -> Unit,
    onLinkGoogle: () -> Unit
) {
    val league = League.fromRating(user.rating)

    Column(
    ){
        Card( // <- this will be user's banner
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ){
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if(isOwner){
                    if(user.email == null){
                        Button(onClick = {onLinkGoogle()}) {
                            Text("Save with Google")
                        }
                    }
                }
                Button(
                    onClick = onNavigateBack,
                ) {
                    Text("X")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.submissionsCount.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Submissions",
                    fontSize = 10.sp
                )
            }

            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if(user.email == null){
                        user.name
                    }else{
                        "${user.name} ✓"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Joined ${formatTime(user.joinedDate)}",
                    fontSize = 10.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.rating.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if(pantheonPosition != null){
                    Text(
                        text = "Pantheon #$pantheonPosition",
                        fontSize = 10.sp
                    )
                }else{
                    Text(
                        text = league.displayName,
                        fontSize = 10.sp
                    )
                }
            }
        }



        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ){
            Text(
                text = "Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ){
            Text("Work in progress")
            Spacer(modifier = Modifier.height(60.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ){
            Text(
                text = "Best Paragraph",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ){
            Text("Work in progress")
            Spacer(modifier = Modifier.height(120.dp))
        }

        if(isOwner){
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.Center

            ){
                Button(onClick = onNavigateToSubmissions) {
                    Text("View Submissions")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    Inkr8Theme {
        Profile(
            user = fakeUser,
            pantheonPosition = null,
            isOwner = true,
            onNavigateBack = {},
            onNavigateToSubmissions = {},
            onLinkGoogle = {}
        )
    }
}