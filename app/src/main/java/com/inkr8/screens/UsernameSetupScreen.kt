package com.inkr8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UsernameSetupScreen(
    isSaving: Boolean,
    errorMessage: String?,
    onSubmit: (String) -> Unit,
    checkAvailability: (String, (Boolean) -> Unit) -> Unit,
    validateUsername: (String) -> String?
) {
    var username by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf<Boolean?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        availability = null
        validationError = null

        if (username.isBlank()) return@LaunchedEffect

        val validation = validateUsername(username)
        if (validation != null) {
            validationError = validation
            return@LaunchedEffect
        }

        isChecking = true
        kotlinx.coroutines.delay(500)

        checkAvailability(username) { available ->
            availability = available
            isChecking = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Choose your name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "This name will represent you inside Inkr8.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = username,
                    onValueChange = {if (it.length <= 20) username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Allowed: letters, digits, _ . , * { } [ ] ( ) √",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                when {
                    validationError != null -> {
                        Text(validationError!!, color = MaterialTheme.colorScheme.error)
                    }
                    isChecking -> {
                        Text("Checking...", color = Color.Gray)
                    }
                    availability == true -> {
                        Text("Available", color = Color(0xFF4CAF50))
                    }
                    availability == false -> {
                        Text("Already taken", color = MaterialTheme.colorScheme.error)
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { onSubmit(username.trim()) },
                    enabled = !isSaving && availability == true && validationError == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) "Claiming..." else "Claim Username")
                }
            }
        }
    }
}
