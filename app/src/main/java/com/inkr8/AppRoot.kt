package com.inkr8

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivity
import com.inkr8.data.*
import com.inkr8.repository.*
import com.inkr8.screens.*
import com.inkr8.economy.*
import com.inkr8.rating.*
import kotlinx.coroutines.delay


@Composable
fun AppRoot(
    initialUser: Users,
    googleLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    var currentUser by remember { mutableStateOf(initialUser) }
    val context = LocalContext.current
    var pantheonPosition by remember { mutableStateOf<Int?>(null) }
    val submissionRepository = remember { FirestoreSubmissionRepository() }
    val tournamentRepository = remember { FirestoreTournamentRepository() }
    val userRepository = remember { UserRepository() }
    var currentGamemode by remember { mutableStateOf<Gamemode?>(null) }
    var currentPlayMode by remember { mutableStateOf<PlayMode>(PlayMode.Practice) }
    var selectedTournament by remember { mutableStateOf<Tournament?>(null) }
    var currentScreen by remember { mutableStateOf(Screen.home) }
    var selectedProfileUserId by remember { mutableStateOf<String?>(null) }
    var activeTournamentId by remember { mutableStateOf<String?>(null) }
    var latestSubmission by remember { mutableStateOf<Submissions?>(null) }
    var submissionAdCounter by remember { mutableStateOf(0) }
    var pendingNavigationAfterAd by remember { mutableStateOf<Screen?>(null) }

    LaunchedEffect(currentUser.id, currentUser.rating, currentUser.currentlyInRanked) {

        if (currentUser.currentlyInRanked) {
            val newRep = ReputationManager.onRankedAbandoned(currentUser.reputation)

            userRepository.updateReputation(currentUser.id, newRep)
            userRepository.finishRankedSession(currentUser.id)

            userRepository.getUserById(currentUser.id) { updatedUser ->
                updatedUser?.let { currentUser = it }
            }
        }

        if (currentUser.rating >= PantheonManager.MIN_RATING) {
            userRepository.getTop100Users { top100 ->
                val (isPantheon, position) =
                    PantheonManager.checkPantheonStatus(currentUser, top100)

                pantheonPosition = if (isPantheon) position else null
            }
        } else {
            pantheonPosition = null
        }
    }

    when(currentScreen) {
        Screen.home -> MainPagerScreen(
            user = currentUser,
            pantheonPosition = pantheonPosition,

            onNavigateToProfile = {
                currentScreen = Screen.profile
            },

            onNavigateToLeaderboard = {
                currentScreen = Screen.leaderboard
            },

            onNavigateToWriting = { gamemode, playMode, tournament ->

                currentGamemode = gamemode
                currentPlayMode = playMode
                selectedTournament = tournament
                latestSubmission = null
                activeTournamentId = tournament?.id

                currentScreen = Screen.writing
            }
        )
        Screen.practice -> {
            currentScreen = Screen.home
        }

        Screen.competitions -> {
            currentScreen = Screen.home
        }
        Screen.writing -> Writing(
            gamemode = currentGamemode ?: StandardWriting,
            playMode = currentPlayMode,
            tournamentContext = if (currentPlayMode is PlayMode.Tournament) selectedTournament else null,
            onAddSubmission = { submission: Submissions ->
                val submissionWithAuthor = submission.copy(
                    authorId = currentUser.id
                )

                val finalSubmission = submissionWithAuthor.copy(
                    status = SubmissionStatus.PENDING,
                    evaluation = null
                )

                val isRanked = finalSubmission.playmode == "RANKED"
                val isTournament = finalSubmission.playmode == "TOURNAMENT" && activeTournamentId != null

                if (isTournament) {
                    tournamentRepository.submitToTournament(
                        tournamentId = activeTournamentId!!,
                        userId = currentUser.id,
                        submission = finalSubmission,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Tournament submission sent",
                                Toast.LENGTH_SHORT
                            ).show()

                            userRepository.getUserById(currentUser.id) { updatedUser ->
                                updatedUser?.let { currentUser = it }
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

                            userRepository.getUserById(currentUser.id) { updatedUser ->
                                updatedUser?.let { currentUser = it }
                            }

                            currentScreen = Screen.loading
                        },
                        onError = { e: Exception ->
                            userRepository.finishRankedSession(currentUser.id)
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

            LaunchedEffect(Unit) {
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
            user = currentUser,
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
            val activity = LocalActivity.current
            var isUnlockingFeedback by remember { mutableStateOf(false) }

            if (latestSubmission != null) {
                Results(
                    submission = latestSubmission!!,
                    isUnlockingFeedback = isUnlockingFeedback,
                    isPhilosopher = currentUser.isPhilosopher,
                    onUnlockFeedback = {
                        val submissionId = latestSubmission!!.id
                        isUnlockingFeedback = true

                        if (currentUser.isPhilosopher) {
                            submissionRepository.unlockFeedbackExpansion(
                                submissionId = submissionId,
                                skipMeritCost = true,
                                onSuccess = {
                                    isUnlockingFeedback = false

                                    submissionRepository.getLastSubmission(
                                        onSuccess = { updatedSubmission ->
                                            latestSubmission = updatedSubmission
                                            Toast.makeText(
                                                context,
                                                "Expanded for free",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { e ->
                                            e.printStackTrace()
                                        }
                                    )
                                },
                                onError = { e ->
                                    isUnlockingFeedback = false
                                    Toast.makeText(
                                        context,
                                        e.message ?: "Failed to expand feedback",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {
                            submissionRepository.unlockFeedbackExpansion(
                                submissionId = submissionId,
                                skipMeritCost = false,
                                onSuccess = { cost ->
                                    isUnlockingFeedback = false

                                    submissionRepository.getLastSubmission(
                                        onSuccess = { updatedSubmission ->
                                            latestSubmission = updatedSubmission
                                            Toast.makeText(
                                                context,
                                                "Expanded for $cost Merit",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { e ->
                                            e.printStackTrace()
                                        }
                                    )
                                },
                                onError = { e ->
                                    isUnlockingFeedback = false
                                    Toast.makeText(
                                        context,
                                        e.message ?: "Failed to expand feedback",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    },
                    onNavigateBack = {
                        if (currentUser.isPhilosopher) {
                            currentScreen = Screen.home
                        } else {
                            submissionAdCounter++
                            pendingNavigationAfterAd = Screen.home

                            if (submissionAdCounter % 2 == 0) {
                                activity?.let {
                                    AdManager.showAd(it)
                                    AdManager.loadAd(it)
                                }
                                currentScreen = Screen.home
                            } else {
                                currentScreen = Screen.postSubmissionAd
                            }
                        }
                    },
                    onNavigateToPractice = {
                        if (currentUser.isPhilosopher) {
                            currentScreen = Screen.home
                        } else {
                            submissionAdCounter++
                            pendingNavigationAfterAd = Screen.practice

                            if (submissionAdCounter % 2 == 0) {
                                activity?.let {
                                    AdManager.showAd(it)
                                    AdManager.loadAd(it)
                                }
                                currentScreen = Screen.home
                            } else {
                                currentScreen = Screen.postSubmissionAd
                            }
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No result available.")
                }
            }
        }
        Screen.leaderboard -> LeaderboardScreen(
            currentUser = currentUser,
            onNavigateBack = { currentScreen = Screen.competitions },
            onUserClick = { user ->
                selectedProfileUserId = user.id
                currentScreen = Screen.profile
            }
        )
        Screen.tournamentDetails -> {
            val initialTournament = selectedTournament

            var liveTournament by remember(initialTournament?.id) { mutableStateOf(initialTournament) }
            var isEnrolled by remember(initialTournament?.id, currentUser.id) { mutableStateOf(false) }
            var isSubmitted by remember(initialTournament?.id, currentUser.id) { mutableStateOf(false) }
            var isEnrolling by remember(initialTournament?.id, currentUser.id) { mutableStateOf(false) }
            var completedLeaderboard by remember(initialTournament?.id) { mutableStateOf<List<TournamentLeaderboardEntry>>(emptyList()) }

            DisposableEffect(initialTournament?.id, currentUser.id) {
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
                        userId = currentUser.id,
                        onUpdate = { enrolled ->
                            isEnrolled = enrolled
                        },
                        onError = { e: Exception  ->
                            e.printStackTrace()
                        }
                    )

                    val submissionRegistration = tournamentRepository.listenToSubmissionStatus(
                        tournamentId = initialTournament.id,
                        userId = currentUser.id,
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

                                userRepository.getUserById(currentUser.id) { updatedUser ->
                                    updatedUser?.let { currentUser = it }
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
                    onOpenSubmission = { submission ->
                        latestSubmission = submission
                        currentScreen = Screen.results
                    }
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
                    isOwner = viewedUser!!.id == currentUser.id,
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
            val activity = LocalActivity.current
            val tournament = selectedTournament
            var leaderboard by remember(tournament?.id) { mutableStateOf<List<TournamentLeaderboardEntry>>(emptyList()) }
            var isLoading by remember(tournament?.id) { mutableStateOf(true) }

            fun continueWithAd(nextScreen: Screen, beforeNavigate: (() -> Unit)? = null) {
                if (currentUser.isPhilosopher) {
                    beforeNavigate?.invoke()
                    currentScreen = nextScreen
                    return
                }
                submissionAdCounter++
                pendingNavigationAfterAd = nextScreen

                if (submissionAdCounter % 2 == 0) {
                    activity?.let {
                        AdManager.showAd(it)
                        AdManager.loadAd(it)
                    }
                    beforeNavigate?.invoke()
                    currentScreen = nextScreen
                } else {
                    beforeNavigate?.invoke()
                    currentScreen = Screen.postSubmissionAd
                }
            }

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
                    currentUserId = currentUser.id,
                    onNavigateBack = {
                        continueWithAd(Screen.tournamentDetails)
                    },
                    onTipUser = { recipientId, amount ->
                        tournamentRepository.sendTournamentTip(
                            tournamentId = tournament.id,
                            tipperId = currentUser.id,
                            recipientId = recipientId,
                            amount = amount,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "$amount Merit sent",
                                    Toast.LENGTH_SHORT
                                ).show()

                                userRepository.getUserById(currentUser.id) { updatedUser ->
                                    updatedUser?.let { currentUser = it }
                                }

                                continueWithAd(Screen.tournamentResults)
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
                        continueWithAd(
                            nextScreen = Screen.userProfile,
                            beforeNavigate = {
                                selectedProfileUserId = userId
                            }
                        )
                    }
                )
            } else {
                continueWithAd(Screen.competitions)
            }
        }
        Screen.loading -> {
            var isResolved by remember { mutableStateOf(false) }

            LaunchedEffect(currentUser.id) {
                submissionRepository.getLastSubmissionRealtime(
                    onUpdate = { submission ->
                        if (submission.status == SubmissionStatus.EVALUATED && !isResolved) {
                            isResolved = true
                            latestSubmission = submission
                            currentScreen = Screen.results
                        }
                    },
                    onError = { it.printStackTrace() }
                )
                while (!isResolved) {
                    delay(2000)
                    submissionRepository.getLastSubmission(
                        onSuccess = { submission ->
                            if (submission != null &&
                                submission.status == SubmissionStatus.EVALUATED &&
                                !isResolved
                            ) {
                                isResolved = true
                                latestSubmission = submission
                                currentScreen = Screen.results
                            }
                        },
                        onError = { it.printStackTrace() }
                    )
                }
            }
            LoadingScreen()
        }
        Screen.createTournament -> {
            CreateTournamentScreen(
                user = currentUser,
                onCreate = { title, gamemode, prizePool, maxPlayers ->

                    tournamentRepository.createUserTournament(
                        title,
                        gamemode,
                        prizePool,
                        maxPlayers,
                        onSuccess = {
                            currentScreen = Screen.competitions
                        },
                        onError = {
                            it.printStackTrace()
                        }
                    )
                },
                onBack = { currentScreen = Screen.competitions }
            )
        }
        Screen.postSubmissionAd -> {
            PostSubmissionAdScreen(
                onContinue = {
                    val next = pendingNavigationAfterAd
                    pendingNavigationAfterAd = null
                    currentScreen = next ?: Screen.home
                },
                onGoAdFree = {
                    currentScreen = Screen.paywall
                }
            )
        }
        Screen.paywall -> {
            PaywallScreen(
                onBack = { currentScreen = Screen.home },
                onSubscribe = {
                    userRepository.enablePhilosopher(
                        userId = currentUser.id,
                        onSuccess = {
                            userRepository.getUserById(currentUser.id) { updatedUser ->
                                updatedUser?.let { currentUser = it }
                            }
                            Toast.makeText(
                                context,
                                "test philosoper",
                                Toast.LENGTH_SHORT
                            ).show()
                            currentScreen = Screen.home
                        },
                        onError = { e ->
                            Toast.makeText(
                                context,
                                e.message ?: "Failed to enable Philosopher",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            )
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
    userProfile,
    loading,
    createTournament,
    postSubmissionAd,
    paywall
}