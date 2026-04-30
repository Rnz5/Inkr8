package com.inkr8

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import com.inkr8.data.*
import com.inkr8.repository.*
import com.inkr8.screens.*
import com.inkr8.economy.*
import com.inkr8.rating.*
import kotlinx.coroutines.delay


@Composable
fun AppRoot(
    initialUser: Users,
    googleLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    onSessionEnded: () -> Unit
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
    var submissionAdCounter by remember { mutableIntStateOf(0) }
    var pendingNavigationAfterAd by remember { mutableStateOf<Screen?>(null) }
    var pagerInitialPage by remember { mutableIntStateOf(1) }

    var allSubmissions by remember { mutableStateOf<List<Submissions>>(emptyList()) }
    var isLoadingSubmissions by remember { mutableStateOf(true) }

    DisposableEffect(currentUser.id) {
        isLoadingSubmissions = true
        val registration = submissionRepository.listenToAllSubmissions(
            authorId = currentUser.id,
            onUpdate = { updatedSubmissions ->
                allSubmissions = updatedSubmissions
                isLoadingSubmissions = false
            },
            onError = { error ->
                error.printStackTrace()
                isLoadingSubmissions = false
            }
        )
        onDispose {
            registration.remove()
        }
    }

    LaunchedEffect(currentUser.id, currentUser.rating) {
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
            initialPage = pagerInitialPage,
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
            },
            onNavigateToTournamentDetails = { tournament ->
                selectedTournament = tournament
                currentScreen = Screen.tournamentDetails
            },
            onNavigateToUserProfile = { userId ->
                selectedProfileUserId = userId
                currentScreen = Screen.userProfile
            },
            onNavigateToCreateTournament = {
                currentScreen = Screen.createTournament
            }
        )
        Screen.practice -> {
            pagerInitialPage = 0
            currentScreen = Screen.home
        }

        Screen.competitions -> {
            pagerInitialPage = 2
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
                    pagerInitialPage = 1
                    Screen.home
                }
            },
            onNavigateToResults = { currentScreen = Screen.results }
        )
        Screen.submissions -> {
            SubmissionsScreen(
                user = currentUser,
                submissions = allSubmissions,
                isLoading = isLoadingSubmissions,
                onNavigateToProfile = { currentScreen = Screen.profile },
                onSaveSubmission = { submissionId ->
                    submissionRepository.saveSubmission(
                        submissionId = submissionId,
                        onSuccess = {
                            allSubmissions = allSubmissions.map {
                                if (it.id == submissionId) it.copy(isSaved = true) else it 
                            }
                            
                            userRepository.getUserById(currentUser.id) { updatedUser ->
                                updatedUser?.let { currentUser = it }
                            }
                        },
                        onError = { e ->
                            Toast.makeText(context, e.message ?: "Failed to save", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
        Screen.savedSubmissions -> {
            SavedSubmissionsScreen(
                savedSubmissions = allSubmissions.filter { it.isSaved },
                isLoading = isLoadingSubmissions,
                onNavigateBack = { currentScreen = Screen.profile },
                onDeleteSubmission = { submissionId ->
                    submissionRepository.deleteSubmission(
                        submissionId = submissionId,
                        onSuccess = {
                            allSubmissions = allSubmissions.filter { it.id != submissionId }
                            Toast.makeText(context, "Entry Dissolved", Toast.LENGTH_SHORT).show()
                        },
                        onError = { e ->
                            Toast.makeText(context, e.message ?: "Failed to dissolve", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
        Screen.profile -> Profile(
            user = currentUser,
            isOwner = true,
            pantheonPosition = pantheonPosition,
            onNavigateBack = { 
                pagerInitialPage = 1
                currentScreen = Screen.home 
            },
            onNavigateToSubmissions = { currentScreen = Screen.submissions },
            onNavigateToSavedSubmissions = { currentScreen = Screen.savedSubmissions },
            onLinkGoogle = {
                val signInIntent = AuthManager.getGoogleSignInIntent()
                googleLauncher.launch(signInIntent)
            },
            onLogout = {
                AuthManager.signOut()
                onSessionEnded()
            },
            onDeleteAccount = {
                userRepository.deleteAccount(
                    userId = currentUser.id,
                    onSuccess = {
                        AuthManager.signOut()
                        onSessionEnded()
                    },
                    onError = { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onChangeUsername = {
                currentScreen = Screen.usernameSetup
            },
            onPurchaseReputation = { onSuccess ->
                userRepository.applyMeritAction(
                    action = "PURCHASE_REPUTATION_VIEW",
                    onSuccess = {
                        userRepository.getUserById(currentUser.id) { updatedUser ->
                            updatedUser?.let { currentUser = it }
                            onSuccess()
                        }
                    },
                    onError = { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onExpandCap = {
                userRepository.applyMeritAction(
                    action = "EXPAND_MERIT_CAP",
                    onSuccess = {
                        userRepository.getUserById(currentUser.id) { updatedUser ->
                            updatedUser?.let { currentUser = it }
                            Toast.makeText(context, "Cap Expanded", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { e ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                )
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

                        submissionRepository.unlockFeedbackExpansion(
                            submissionId = submissionId,
                            skipMeritCost = currentUser.isPhilosopher,
                            onSuccess = { cost ->
                                isUnlockingFeedback = false
                                // Optimistically update latestSubmission
                                latestSubmission = latestSubmission?.let { current ->
                                    current.copy(
                                        evaluation = current.evaluation?.copy(feedbackUnlocked = true)
                                    )
                                }

                                userRepository.getUserById(currentUser.id) { updatedUser ->
                                    updatedUser?.let { currentUser = it }
                                }

                                Toast.makeText(
                                    context,
                                    if (currentUser.isPhilosopher) "Decrypted" else "Decrypted for $cost Merit",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    },
                    onNavigateBack = {
                        if (currentUser.isPhilosopher) {
                            pagerInitialPage = 1
                            currentScreen = Screen.home
                        } else {
                            submissionAdCounter++
                            pendingNavigationAfterAd = Screen.home

                            if (submissionAdCounter % 2 == 0) {
                                activity?.let {
                                    AdManager.showAd(it)
                                }
                                pagerInitialPage = 1
                                currentScreen = Screen.home
                            } else {
                                currentScreen = Screen.postSubmissionAd
                            }
                        }
                    },
                    onNavigateToPractice = {
                        if (currentUser.isPhilosopher) {
                            pagerInitialPage = 0
                            currentScreen = Screen.home
                        } else {
                            submissionAdCounter++
                            pendingNavigationAfterAd = Screen.practice

                            if (submissionAdCounter % 2 == 0) {
                                activity?.let {
                                    AdManager.showAd(it)
                                }
                                pagerInitialPage = 0
                                currentScreen = Screen.home
                            } else {
                                currentScreen = Screen.postSubmissionAd
                            }
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No result available.", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                pagerInitialPage = 1
                                currentScreen = Screen.home 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                        ) {
                            Text("Return Home", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        Screen.leaderboard -> LeaderboardScreen(
            currentUser = currentUser,
            onNavigateBack = { 
                pagerInitialPage = 2
                currentScreen = Screen.home 
            },
            onUserClick = { user ->
                selectedProfileUserId = user.id
                currentScreen = Screen.userProfile
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
                    onNavigateBack = { 
                        pagerInitialPage = 2
                        currentScreen = Screen.home 
                    },
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
                pagerInitialPage = 2
                currentScreen = Screen.home
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
                    onNavigateBack = { 
                        pagerInitialPage = 2
                        currentScreen = Screen.home 
                    },
                    onNavigateToSubmissions = { currentScreen = Screen.submissions },
                    onNavigateToSavedSubmissions = { currentScreen = Screen.savedSubmissions },
                    onLinkGoogle = {},
                    onLogout = {},
                    onDeleteAccount = {},
                    onChangeUsername = {},
                    onPurchaseReputation = { onSuccess ->
                        userRepository.applyMeritAction(
                            action = "PURCHASE_REPUTATION_VIEW",
                            onSuccess = {
                                userRepository.getUserById(currentUser.id) { updatedUser ->
                                    updatedUser?.let { currentUser = it }
                                    onSuccess()
                                }
                            },
                            onError = { e ->
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onExpandCap = {}
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
                pagerInitialPage = 2
                currentScreen = Screen.home
            }
        }
        Screen.loading -> {
            var isResolved by remember { mutableStateOf(false) }

            LaunchedEffect(currentUser.id) {
                submissionRepository.getLastSubmissionRealtime(
                    onUpdate = { submission ->
                        if (!isResolved) {
                            if (submission.status == SubmissionStatus.EVALUATED) {
                                isResolved = true
                                latestSubmission = submission
                                currentScreen = Screen.results
                            } else if (submission.status == SubmissionStatus.FAILED) {
                                isResolved = true
                                Toast.makeText(context, "Did R8 fail to judge this entry?.", Toast.LENGTH_LONG).show()
                                currentScreen = Screen.home
                            }
                        }
                    },
                    onError = { 
                        it.printStackTrace()
                        if (it.message?.contains("index") == true) {
                            Toast.makeText(context, "Critical Error, Contact support post-haste!.", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                while (!isResolved) {
                    delay(3000)
                    submissionRepository.getLastSubmission(
                        onSuccess = { submission ->
                            if (submission != null && !isResolved) {
                                if (submission.status == SubmissionStatus.EVALUATED) {
                                    isResolved = true
                                    latestSubmission = submission
                                    currentScreen = Screen.results
                                } else if (submission.status == SubmissionStatus.FAILED) {
                                    isResolved = true
                                    Toast.makeText(context, "Evaluation failed.", Toast.LENGTH_LONG).show()
                                    currentScreen = Screen.home
                                }
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
                            pagerInitialPage = 2
                            currentScreen = Screen.home
                        },
                        onError = {
                            it.printStackTrace()
                        }
                    )
                },
                onBack = { 
                    pagerInitialPage = 2
                    currentScreen = Screen.home 
                }
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
                onBack = { 
                    pagerInitialPage = 1
                    currentScreen = Screen.home 
                },
                onSubscribe = {
                    userRepository.enablePhilosopher(
                        onSuccess = {
                            userRepository.getUserById(currentUser.id) { updatedUser ->
                                updatedUser?.let { currentUser = it }
                            }
                            Toast.makeText(
                                context,
                                "test philosoper",
                                Toast.LENGTH_SHORT
                            ).show()
                            pagerInitialPage = 1
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
        Screen.usernameSetup -> {
            UsernameSetupScreen(
                isSaving = false,
                errorMessage = null,
                onSubmit = { newName ->
                    userRepository.changeUsernameWithMerit(
                        newUsername = newName,
                        onSuccess = {
                            userRepository.getUserById(currentUser.id) { updatedUser ->
                                updatedUser?.let { currentUser = it }
                                currentScreen = Screen.profile
                            }
                        },
                        onError = { e ->
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                checkAvailability = { name, callback ->
                    userRepository.isUsernameAvailable(name, callback)
                },
                validateUsername = { name ->
                    userRepository.validateUsername(name)
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
    savedSubmissions,
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
    paywall,
    usernameSetup
}
