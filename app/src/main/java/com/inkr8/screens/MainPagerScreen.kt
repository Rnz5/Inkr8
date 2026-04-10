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
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToWriting: (Gamemode, PlayMode, Tournament?) -> Unit
) {

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        when (page) {

            0 -> Practice(
                user = user,
                pantheonPosition = pantheonPosition,
                onNavigateBack = { /* ignore */ },
                onNavigateToWriting = { gamemode ->
                    onNavigateToWriting(gamemode, PlayMode.Practice, null)
                },
                onNavigateToProfile = onNavigateToProfile
            )

            1 -> HomeScreen(
                user = user,
                pantheonPosition = pantheonPosition,
                onNavigateToPractice = {
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                },
                onNavigateToCompetitions = {
                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                },
                onNavigateToProfile = onNavigateToProfile
            )

            2 -> Competitions(
                user = user,
                pantheonPosition = pantheonPosition,
                onNavigateBack = { /* ignore */ },
                onNavigateToWriting = { gamemode ->
                    onNavigateToWriting(gamemode, PlayMode.Ranked, null)
                },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onNavigateToTournamentDetails = { /* handled outside later */ },
                onNavigateToUserProfile = { /* handled outside later */ },
                onNavigateToCreateTournament = { /* handled outside later */ }
            )
        }
    }
}