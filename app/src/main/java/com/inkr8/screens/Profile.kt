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
import com.inkr8.R
import com.inkr8.ui.theme.Inkr8Theme
@Composable
fun Profile(
    onNavigateBack: () -> Unit,
    onNavigateToSubmissions: () -> Unit,
) {

    Column(
    ){
        Card( // <- this will be user's banner
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ){

            Spacer(modifier = Modifier.height(60.dp))
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
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ){
            Text(
                text = "Username",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )

            Text(
                text = "pronouns",
                fontSize = 12.sp,
                modifier = Modifier.padding(4.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.End
            ){
                Text(
                    text = "Rank",
                    modifier = Modifier.padding(4.dp)
                )
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.Center

        ){
            Button(onClick = onNavigateToSubmissions) {
                Text("View Submissions")
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = onNavigateBack,
        ) {
            Text("X")
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    Inkr8Theme {
        Profile(
            onNavigateBack = {},
            onNavigateToSubmissions = {}
        )
    }
}