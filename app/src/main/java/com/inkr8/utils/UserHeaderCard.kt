package com.inkr8.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.rating.League
import com.inkr8.rating.PantheonManager


@Composable
fun UserHeaderCard(
    user: Users,
    pantheonPosition: Int?,
    onClick: () -> Unit
) {

    val league = League.fromRating(user.rating)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.pfpexample),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(text = user.name)
                    Text(text = "Merit: ${user.merit}")
                }

                Spacer(modifier = Modifier.weight(1f))

                if (pantheonPosition != null) {
                    Text(text = "Pantheon #$pantheonPosition")
                } else {
                    Text(text = league.displayName)
                }
            }
        }
    }
}
