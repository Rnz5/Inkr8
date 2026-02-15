package com.inkr8

import android.content.Intent
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
import androidx.compose.ui.Alignment
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
import com.inkr8.evaluation.SubmissionFactory
import com.inkr8.repository.FirestoreSubmissionRepository
import com.inkr8.repository.UserRepository

class MainActivity : ComponentActivity() {

    private lateinit var googleLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private val userRepository = UserRepository()
    private var currentUserState = mutableStateOf<Users?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthManager.initGoogle(this)

        googleLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                AuthManager.linkWithGoogle(
                    result.data,
                    onSuccess = { firebaseUser ->

                        userRepository.updateEmail(
                            userId = firebaseUser.uid,
                            email = firebaseUser.email
                        )

                        userRepository.getUserById(firebaseUser.uid) { user ->
                            currentUserState.value = user
                        }
                    },
                    onError = { it.printStackTrace() }
                )
            }
        }
        setContent {
            var currentUser by currentUserState


            LaunchedEffect(Unit) {

                val firebaseUser = AuthManager.currentUser()

                if (firebaseUser != null) {

                    userRepository.updateEmail(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email
                    )

                    userRepository.ensureUserExists(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "User${firebaseUser.uid.take(4)}",
                        email = firebaseUser.email
                    ) { user -> currentUser = user }

                } else {
                    AuthManager.signInAnonymously(
                        onSuccess = { firebaseUserAnon ->
                            userRepository.ensureUserExists(
                                uid = firebaseUserAnon.uid,
                                name = "User${firebaseUserAnon.uid.take(4)}",
                                email = null
                            ) { user -> currentUser = user }
                        },
                        onError = { it.printStackTrace() }
                    )
                }
            }

            if (currentUser == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Signing in...")
                }
                return@setContent
            }

            val submissionRepository = remember { FirestoreSubmissionRepository() }
            val submissionProcessor = remember { SubmissionProcessor(FakeEvaluator()) }


            Inkr8Theme {
                var currentGamemode by remember { mutableStateOf<Gamemode?>(null) }
                var currentScreen by remember { mutableStateOf(Screen.home) }

                when(currentScreen) {
                    Screen.home -> HomeScreen(
                        user = currentUser!!,
                        onNavigateToPractice = { currentScreen = Screen.practice },
                        onNavigateToCompetitions = { currentScreen = Screen.competitions },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.practice -> Practice(
                        user = currentUser!!,
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->
                            currentGamemode = gamemode
                            currentScreen = Screen.writing },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.competitions -> Competitions(
                        user = currentUser!!,
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->
                            currentGamemode = gamemode
                            currentScreen = Screen.writing
                        },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.writing -> Writing(
                        gamemode = currentGamemode ?: StandardWriting,
                        onAddSubmission = { submission: Submissions ->
                            val submissionWithAuthor = submission.copy(
                                authorId = currentUser!!.id
                            )

                            val finalSubmission = submissionProcessor.process(submissionWithAuthor)

                            submissionRepository.addSubmission(
                                submission = finalSubmission,
                                onSuccess = {

                                    val meritEarned = finalSubmission.evaluation?.meritEarned ?: 0
                                    val newScore = finalSubmission.evaluation?.finalScore ?: 0.0

                                    userRepository.addMerit(
                                        userId = currentUser!!.id,
                                        amount = meritEarned.toLong()
                                    )

                                    userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                        currentUser = updatedUser
                                    }

                                    currentScreen = Screen.results
                                },
                                onError = { e: Exception -> e.printStackTrace() }
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
                        user = currentUser!!,
                        isOwner = true,
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToSubmissions = { currentScreen = Screen.submissions },
                        onLinkGoogle = {
                            val signInIntent = AuthManager.getGoogleSignInIntent()
                            googleLauncher.launch(signInIntent)
                        }
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