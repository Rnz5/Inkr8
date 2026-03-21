package com.inkr8

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.inkr8.data.Gamemode
import com.inkr8.data.OnTopicWriting
import com.inkr8.data.PlayMode
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
import com.inkr8.data.Theme
import com.inkr8.data.Topic
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentLeaderboardEntry
import com.inkr8.data.TournamentStatus
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.economy.RankedCostCalculator
import com.inkr8.rating.PantheonManager
import com.inkr8.rating.RatingCalculator
import com.inkr8.rating.ReputationManager
import com.inkr8.repository.FirestoreSubmissionRepository
import com.inkr8.repository.FirestoreTournamentRepository
import com.inkr8.repository.UserRepository
import com.inkr8.screens.LeaderboardScreen
import com.inkr8.screens.TournamentDetails
import com.inkr8.screens.TournamentResultsScreen

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
            val context = LocalContext.current
            var pantheonPosition by remember { mutableStateOf<Int?>(null) }



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

            LaunchedEffect(currentUser?.id, currentUser?.rating) {

                val user = currentUser ?: return@LaunchedEffect

                if (currentUser!!.currentlyInRanked) {

                    val newRep = ReputationManager.onRankedAbandoned(currentUser!!.reputation)

                    userRepository.updateReputation(currentUser!!.id, newRep)
                    userRepository.finishRankedSession(currentUser!!.id)
                }

                if (user.rating >= PantheonManager.MIN_RATING) {
                    userRepository.getTop100Users { top100 ->
                        val (isPantheon, position) =
                            PantheonManager.checkPantheonStatus(user, top100)
                        pantheonPosition = if (isPantheon) position else null
                    }
                } else {
                    pantheonPosition = null
                }
            }

            val submissionRepository = remember { FirestoreSubmissionRepository() }
            val submissionProcessor = remember { SubmissionProcessor(FakeEvaluator()) }
            val tournamentRepository = remember { FirestoreTournamentRepository() }


            Inkr8Theme {
                var currentGamemode by remember { mutableStateOf<Gamemode?>(null) }
                var currentPlayMode by remember { mutableStateOf<PlayMode>(PlayMode.Practice) }
                var selectedTournament by remember { mutableStateOf<Tournament?>(null) }
                var currentScreen by remember { mutableStateOf(Screen.home) }
                var selectedProfileUserId by remember { mutableStateOf<String?>(null) }
                var activeTournamentId by remember { mutableStateOf<String?>(null) }


                when(currentScreen) {
                    Screen.home -> HomeScreen(
                        user = currentUser!!,
                        pantheonPosition = pantheonPosition,
                        onNavigateToPractice = { currentScreen = Screen.practice },
                        onNavigateToCompetitions = { currentScreen = Screen.competitions },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.practice -> Practice(
                        user = currentUser!!,
                        pantheonPosition = pantheonPosition,
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->
                            activeTournamentId = null
                            currentGamemode = gamemode
                            currentPlayMode = PlayMode.Practice
                            currentScreen = Screen.writing
                        },
                        onNavigateToProfile = { currentScreen = Screen.profile }
                    )
                    Screen.competitions -> Competitions(
                        user = currentUser!!,
                        pantheonPosition = pantheonPosition,
                        onNavigateBack = { currentScreen = Screen.home },
                        onNavigateToWriting = { gamemode ->

                            val entryCost = RankedCostCalculator.calculateCost(
                                EconomyConfig.BASE_COST_RANKED,
                                currentUser!!.rankedWinStreak,
                                currentUser!!.rankedLossStreak,
                                currentUser!!.reputation
                            )

                            if (currentUser!!.merit >= entryCost) {
                                userRepository.applyMeritAction(
                                    action = "ENTER_RANKED",
                                    onSuccess = {
                                        userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                            currentUser = updatedUser
                                        }

                                        activeTournamentId = null
                                        currentGamemode = gamemode
                                        currentPlayMode = PlayMode.Ranked
                                        currentScreen = Screen.writing
                                    },
                                    onError = { e ->
                                        Toast.makeText(
                                            context,
                                            e.message ?: "Failed to enter ranked",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Not enough Merit",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onNavigateToProfile = { currentScreen = Screen.profile },
                        onNavigateToLeaderboard = { currentScreen = Screen.leaderboard },
                        onNavigateToTournamentDetails = { tournament ->
                            selectedTournament = tournament
                            currentScreen = Screen.tournamentDetails
                        },
                        onNavigateToUserProfile = { userId ->
                            selectedProfileUserId = userId
                            currentScreen = Screen.userProfile
                        }
                    )
                    Screen.writing -> Writing(
                        gamemode = currentGamemode ?: StandardWriting,
                        playMode = currentPlayMode,
                        tournamentContext = if (currentPlayMode is PlayMode.Tournament) selectedTournament else null,
                        onAddSubmission = { submission: Submissions ->
                            val submissionWithAuthor = submission.copy(
                                authorId = currentUser!!.id
                            )

                            val finalSubmission = submissionProcessor.process(submissionWithAuthor)

                            val isRanked = finalSubmission.playmode == "RANKED"
                            val isTournament = finalSubmission.playmode == "TOURNAMENT" && activeTournamentId != null

                            if (isTournament) {
                                tournamentRepository.submitToTournament(
                                    tournamentId = activeTournamentId!!,
                                    userId = currentUser!!.id,
                                    submission = finalSubmission,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Tournament submission sent",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                            currentUser = updatedUser
                                        }

                                        currentScreen = Screen.tournamentDetails
                                    },
                                    onError = { e: Exception ->
                                        Toast.makeText(
                                            context,
                                            e.message ?: "Failed to submit to tournament",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        e.printStackTrace()
                                    }
                                )
                            } else {
                                submissionRepository.addSubmission(
                                    submission = finalSubmission,
                                    onSuccess = {

                                        if (isRanked) {

                                            val finalScore = finalSubmission.evaluation?.finalScore ?: 0.0

                                            val newRating = RatingCalculator.calculateNewRating(
                                                currentRating = currentUser!!.rating,
                                                score = finalScore
                                            )

                                            val isWin = finalScore >= 60.0
                                            val newWinStreak = if (isWin) currentUser!!.rankedWinStreak + 1 else 0

                                            var rep = currentUser!!.reputation

                                            rep = ReputationManager.onRankedCompleted(rep)

                                            if (newWinStreak > 0 && newWinStreak % 5 == 0L) {
                                                rep = ReputationManager.consistencyBonus(rep)
                                            }

                                            userRepository.updateReputation(currentUser!!.id, rep)

                                            val newLossStreak = if (!isWin) currentUser!!.rankedLossStreak + 1 else 0

                                            userRepository.updateRatingAndStreak(
                                                userId = currentUser!!.id,
                                                newRating = newRating,
                                                winStreak = newWinStreak,
                                                lossStreak = newLossStreak
                                            )

                                            userRepository.finishRankedSession(currentUser!!.id)
                                        }

                                        userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                            currentUser = updatedUser
                                        }

                                        currentScreen = Screen.results
                                    },
                                    onError = { e: Exception ->
                                        userRepository.finishRankedSession(currentUser!!.id)
                                        e.printStackTrace()
                                    }
                                )
                            }
                        },
                        onNavigateBack = {
                            currentScreen = if (activeTournamentId != null && currentPlayMode is PlayMode.Tournament) {
                                Screen.tournamentDetails
                            } else {
                                Screen.home
                            }
                        },
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
                        pantheonPosition = pantheonPosition,
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
                    Screen.leaderboard -> LeaderboardScreen(
                        currentUser = currentUser!!,
                        onNavigateBack = { currentScreen = Screen.competitions }
                    )
                    Screen.tournamentDetails -> {
                        val initialTournament = selectedTournament

                        var liveTournament by remember(initialTournament?.id) { mutableStateOf(initialTournament) }
                        var isEnrolled by remember(initialTournament?.id, currentUser!!.id) { mutableStateOf(false) }
                        var isSubmitted by remember(initialTournament?.id, currentUser!!.id) { mutableStateOf(false) }
                        var isEnrolling by remember(initialTournament?.id, currentUser!!.id) { mutableStateOf(false) }
                        var completedLeaderboard by remember(initialTournament?.id) { mutableStateOf<List<TournamentLeaderboardEntry>>(emptyList()) }

                        DisposableEffect(initialTournament?.id, currentUser!!.id) {
                            if (initialTournament == null) {
                                onDispose {}
                            } else {
                                val tournamentRegistration = tournamentRepository.listenToTournament(
                                    tournamentId = initialTournament.id,
                                    onUpdate = { updatedTournament ->
                                        liveTournament = updatedTournament
                                    },
                                    onError = { e: Exception ->
                                        e.printStackTrace()
                                    }
                                )

                                val enrollmentRegistration = tournamentRepository.listenToEnrollmentStatus(
                                    tournamentId = initialTournament.id,
                                    userId = currentUser!!.id,
                                    onUpdate = { enrolled ->
                                        isEnrolled = enrolled
                                    },
                                    onError = { e: Exception  ->
                                        e.printStackTrace()
                                    }
                                )

                                val submissionRegistration = tournamentRepository.listenToSubmissionStatus(
                                    tournamentId = initialTournament.id,
                                    userId = currentUser!!.id,
                                    onUpdate = { submitted ->
                                        isSubmitted = submitted
                                    },
                                    onError = { e: Exception  ->
                                        e.printStackTrace()
                                    }
                                )

                                onDispose {
                                    tournamentRegistration.remove()
                                    enrollmentRegistration.remove()
                                    submissionRegistration.remove()
                                }
                            }
                        }

                        LaunchedEffect(liveTournament?.id, liveTournament?.status) {
                            val tournament = liveTournament ?: return@LaunchedEffect

                            if (tournament.status == TournamentStatus.COMPLETED) {
                                tournamentRepository.getLeaderboard(
                                    tournamentId = tournament.id,
                                    onSuccess = { results ->
                                        val authorIds = results.map { it.authorId }
                                        userRepository.getUsersByIds(authorIds) { usersMap ->
                                            completedLeaderboard = results.map { submission ->
                                                TournamentLeaderboardEntry(
                                                    submission = submission,
                                                    user = usersMap[submission.authorId]
                                                )
                                            }
                                        }
                                    },
                                    onError = { e ->
                                        e.printStackTrace()
                                        completedLeaderboard = emptyList()
                                    }
                                )
                            } else {
                                completedLeaderboard = emptyList()
                            }
                        }

                        val tournament = liveTournament

                        if (tournament != null) {
                            TournamentDetails(
                                tournament = tournament,
                                onNavigateBack = { currentScreen = Screen.competitions },
                                onHostClick = {
                                    selectedProfileUserId = tournament.creatorId
                                    currentScreen = Screen.userProfile
                                },
                                isEnrolled = isEnrolled,
                                isSubmitted = isSubmitted,
                                isEnrolling = isEnrolling,
                                onEnroll = {
                                    if (isEnrolled || isEnrolling) return@TournamentDetails

                                    isEnrolling = true

                                    tournamentRepository.enrollUserViaFunction(
                                        tournamentId = tournament.id,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Enrolled successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                                currentUser = updatedUser
                                                isEnrolling = false
                                            }
                                        },
                                        onError = { e: Exception ->
                                            Toast.makeText(
                                                context,
                                                e.message ?: "Failed to enroll",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isEnrolling = false
                                        }
                                    )
                                },
                                onSubmitToTournament = {
                                    activeTournamentId = tournament.id
                                    selectedTournament = tournament

                                    currentGamemode = when (tournament.gamemode) {
                                        "ON_TOPIC" -> OnTopicWriting(
                                            theme = Theme(
                                                id = tournament.themeId ?: "",
                                                name = tournament.themeName ?: "Unknown Theme"
                                            ),
                                            topic = Topic(
                                                id = tournament.topicId ?: "",
                                                name = tournament.topicName ?: "Unknown Topic"
                                            )
                                        )
                                        else -> StandardWriting
                                    }

                                    currentPlayMode = PlayMode.Tournament(tournament.id)
                                    currentScreen = Screen.writing
                                },
                                onViewResults = {
                                    selectedTournament = tournament
                                    currentScreen = Screen.tournamentResults
                                },
                                completedLeaderboard = completedLeaderboard,
                            )
                        } else {
                            currentScreen = Screen.competitions
                        }
                    }
                    Screen.userProfile -> {
                        var viewedUser by remember { mutableStateOf<Users?>(null) }
                        var viewedPantheonPosition by remember { mutableStateOf<Int?>(null) }

                        LaunchedEffect(selectedProfileUserId) {
                            val userId = selectedProfileUserId ?: return@LaunchedEffect

                            userRepository.getUserById(userId) { user ->
                                viewedUser = user

                                if (user != null && user.rating >= PantheonManager.MIN_RATING) {
                                    userRepository.getTop100Users { top100 ->
                                        val (isPantheon, position) =
                                            PantheonManager.checkPantheonStatus(user, top100)
                                        viewedPantheonPosition = if (isPantheon) position else null
                                    }
                                } else {
                                    viewedPantheonPosition = null
                                }
                            }
                        }

                        if (viewedUser != null) {
                            Profile(
                                user = viewedUser!!,
                                isOwner = viewedUser!!.id == currentUser!!.id,
                                pantheonPosition = viewedPantheonPosition,
                                onNavigateBack = { currentScreen = Screen.competitions },
                                onNavigateToSubmissions = { currentScreen = Screen.submissions },
                                onLinkGoogle = {}
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Loading profile...")
                            }
                        }
                    }
                    Screen.tournamentResults -> {
                        val tournament = selectedTournament
                        var leaderboard by remember(tournament?.id) { mutableStateOf<List<TournamentLeaderboardEntry>>(emptyList()) }
                        var isLoading by remember(tournament?.id) { mutableStateOf(true) }

                        LaunchedEffect(tournament?.id) {
                            if (tournament == null) {
                                isLoading = false
                                return@LaunchedEffect
                            }

                            tournamentRepository.getLeaderboard(
                                tournamentId = tournament.id,
                                onSuccess = { results ->
                                    val authorIds = results.map { it.authorId }

                                    userRepository.getUsersByIds(authorIds) { usersMap ->
                                        leaderboard = results.map { submission ->
                                            TournamentLeaderboardEntry(
                                                submission = submission,
                                                user = usersMap[submission.authorId]
                                            )
                                        }
                                        isLoading = false
                                    }
                                },
                                onError = { e ->
                                    e.printStackTrace()
                                    leaderboard = emptyList()
                                    isLoading = false
                                }
                            )
                        }

                        if (tournament != null) {
                            TournamentResultsScreen(
                                tournament = tournament,
                                leaderboard = leaderboard,
                                isLoading = isLoading,
                                currentUserId = currentUser!!.id,
                                onNavigateBack = { currentScreen = Screen.tournamentDetails },
                                onTipUser = { recipientId, amount ->
                                    tournamentRepository.sendTournamentTip(
                                        tournamentId = tournament.id,
                                        tipperId = currentUser!!.id,
                                        recipientId = recipientId,
                                        amount = amount,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Tip sent",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                                currentUser = updatedUser
                                            }
                                        },
                                        onError = { e ->
                                            Toast.makeText(
                                                context,
                                                e.message ?: "Failed to send tip",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                onOpenUserProfile = { userId ->
                                    selectedProfileUserId = userId
                                    currentScreen = Screen.userProfile
                                }
                            )
                        } else {
                            currentScreen = Screen.competitions
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    home,
    practice,
    writing,
    submissions,
    competitions,
    profile,
    results,
    leaderboard,
    tournamentDetails,
    tournamentResults,
    userProfile
}