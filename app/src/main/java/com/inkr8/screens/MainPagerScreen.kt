package com.inkr8.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.inkr8.data.Gamemode
import com.inkr8.data.PlayMode
import com.inkr8.data.Tournament
import com.inkr8.data.Users
import kotlinx.coroutines.launch

@Composable
fun MainPagerScreen(
    user: Users,
    pantheonPosition: Int?,
    initialPage: Int = 1,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToWriting: (Gamemode, PlayMode, Tournament?) -> Unit,
    onNavigateToTournamentDetails: (Tournament) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToCreateTournament: () -> Unit
) {

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val goToHome = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        when (page) {

            0 -> Practice(
                user = user,
                pantheonPosition = pantheonPosition,
                onNavigateBack = { goToHome() },
                onNavigateToWriting = { gamemode -> onNavigateToWriting(gamemode, PlayMode.Practice, null) },
                onNavigateToProfile = onNavigateToProfile
            )

            1 -> HomeScreen(
                user = user,
                pantheonPosition = pantheonPosition,
                onNavigateToPractice = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                onNavigateToCompetitions = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                onNavigateToProfile = onNavigateToProfile
            )

            2 -> Competitions(
                user = user,
                pantheonPosition = pantheonPosition,
                onNavigateBack = { goToHome() },
                onNavigateToWriting = { gamemode -> onNavigateToWriting(gamemode, PlayMode.Ranked, null) },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onNavigateToTournamentDetails = onNavigateToTournamentDetails,
                onNavigateToUserProfile = onNavigateToUserProfile,
                onNavigateToCreateTournament = onNavigateToCreateTournament
            )
        }
    }
}