package com.inkr8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.inkr8.data.Gamemode
import com.inkr8.data.StandardWriting
import com.inkr8.evaluation.FakeEvaluator
import com.inkr8.evaluation.SubmissionProcessor
import com.inkr8.screens.Competitions
import com.inkr8.screens.HomeScreen
import com.inkr8.screens.Practice
import com.inkr8.screens.Profile
import com.inkr8.screens.Results
import com.inkr8.screens.Submissions
import com.inkr8.screens.Writing
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.data.Submissions
import com.inkr8.data.Users
import com.inkr8.repository.FirestoreSubmissionRepository
import com.inkr8.repository.UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val userRepository = remember { UserRepository() }
            var currentUser by remember { mutableStateOf<Users?>(null) }

            LaunchedEffect(Unit) {
                AuthManager.ensureSignedIn { firebaseUser ->
                    userRepository.ensureUserExists(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "User${firebaseUser.uid.take(4)}",
                        email = firebaseUser.email
                    ) { user ->
                        currentUser = user
                    }
                }
            }

            if (currentUser == null) {
                Box(modifier = Modifier.fillMaxSize()) {

                }
                return@setContent
            }

            val submissionRepository = remember(currentUser!!.id) {
                FirestoreSubmissionRepository(currentUser!!.id)
            }
            val submissionProcessor = remember { SubmissionProcessor(FakeEvaluator()) }


            Inkr8Theme {
                var currentGamemode by remember { mutableStateOf<Gamemode?>(null) }
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
                    Screen.competitions -> Competitions(
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->
                            currentGamemode = gamemode
                            currentScreen = Screen.writing
                        },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.writing -> Writing(
                        gamemode = currentGamemode ?: StandardWriting,
                        onAddSubmission = { submission ->
                            val finalSubmission = submissionProcessor.process(submission)

                            submissionRepository.addSubmission(submission = finalSubmission,
                                onSuccess = { currentScreen = Screen.results },
                                onError = { e -> e.printStackTrace() }
                            )
                        },
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToResults = { currentScreen = Screen.results }
                    )
                    Screen.submissions -> {
                        var submissions by remember { mutableStateOf<List<Submissions>>(emptyList()) }

                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            submissionRepository.getAllSubmissions(
                                onSuccess = { submissions = it },
                                onError = { e -> e.printStackTrace()
                                    submissions = emptyList()
                                }
                            )
                        }

                        Submissions(
                            submissions = submissions,
                            onNavigateToProfile = { currentScreen = Screen.profile }
                        )
                    }
                    Screen.profile -> Profile(
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToSubmissions = { currentScreen = Screen.submissions }
                    )
                    Screen.results -> {
                        var lastSubmission by remember { mutableStateOf<Submissions?>(null) }

                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            submissionRepository.getLastSubmission(
                                onSuccess = { submission -> lastSubmission = submission },
                                onError = { e -> e.printStackTrace()
                                    lastSubmission = null
                                }
                            )
                        }

                        if (lastSubmission != null) {
                            Results(
                                submission = lastSubmission!!,
                                onNavigateBack = { currentScreen = Screen.home },
                                onNavigateToPractice = { currentScreen = Screen.practice }
                            )
                        }
                    }

                }
            }
        }
    }
}

enum class Screen { home, practice, writing, submissions, competitions, profile, results }