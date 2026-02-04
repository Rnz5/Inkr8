package com.inkr8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.inkr8.data.Gamemode
import com.inkr8.data.Submissions
import com.inkr8.data.standardWriting
import com.inkr8.screens.HomeScreen
import com.inkr8.screens.Practice
import com.inkr8.screens.Profile
import com.inkr8.screens.Submissions
import com.inkr8.screens.Writing
import com.inkr8.ui.theme.Inkr8Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Inkr8Theme {
                var currentGamemode by remember { mutableStateOf<Gamemode?>(null) }
                val userSubmits = remember { mutableStateListOf<Submissions>() }
                var currentScreen by remember { mutableStateOf(Screen.home) }

                when(currentScreen) {
                    Screen.home -> HomeScreen(
                        onNavigateToPractice = { currentScreen = Screen.practice },
                        onNavigateToCompetitions = { currentScreen = Screen.competitions },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.practice -> Practice(
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->
                            currentGamemode = gamemode
                            currentScreen = Screen.writing },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.competitions -> Practice(
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->
                            currentGamemode = gamemode
                            currentScreen = Screen.writing },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.writing -> Writing(
                        gamemode = currentGamemode ?: standardWriting,
                        submissions = userSubmits,
                        onAddSubmission = { text -> userSubmits.add(text) },
                        onNavigateBack = { currentScreen = Screen.home }
                    )
                    Screen.submissions -> Submissions(
                        submissions = userSubmits,
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.profile -> Profile(
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToSubmissions = { currentScreen = Screen.submissions }
                    )

                }
            }
        }
    }
}

enum class Screen { home, practice, writing, submissions, competitions, profile }