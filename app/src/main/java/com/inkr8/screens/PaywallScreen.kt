package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inkr8.ui.theme.Inkr8Theme

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    onSubscribe: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Button(onClick = onBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Become a Philosopher",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = "You are playing the game.\nPhilosophers define it.",
                style = MaterialTheme.typography.bodyMedium
            )


            Spacer(modifier = Modifier.height(24.dp))

            BenefitItem("No Ads")
            BenefitItem("Expanded Feedback — always unlocked")
            BenefitItem("Example Sentence — free")
            BenefitItem("Widget Fee — free")
            BenefitItem("Profile Glow + Badge")

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Philosopher", fontWeight = FontWeight.Bold)
                    Text("Status elevated")
                }
            }
        }

        Column {
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Philosopher")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Stay in the system. Or rise above it.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✔", modifier = Modifier.padding(end = 8.dp))
        Text(text)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PaywallScreenPreview() {
    Inkr8Theme {
        PaywallScreen(
            onBack = {},
            onSubscribe = {}
        )
    }
}