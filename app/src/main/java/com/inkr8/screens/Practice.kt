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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.R
import com.inkr8.ui.theme.Inkr8Theme

@Composable
fun Practice(
    onNavigateBack: () -> Unit,
    onNavigateToWriting: () -> Unit,
    onNavigateToProfile: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ){
        Button(
            onClick = onNavigateToProfile,
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = onNavigateToWriting,
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Text(
                        text = "STANDARD - Writing",
                        fontSize = 24.sp,

                        )
                }
                Text(
                    text = "Write a 150 words long paragraph using 4 special words.",
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
                    onClick = onNavigateToWriting,
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Text(
                        text = "ON-TOPIC - Writing",
                        fontSize = 24.sp,

                        )
                }
                Text(
                    text = "Write a 200 words long paragraph using 4 special words of a special topic.",
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
            onNavigateBack = {},
            onNavigateToWriting = {},
            onNavigateToProfile = {}
        )
    }
}