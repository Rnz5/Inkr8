package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

@Composable
fun PostSubmissionAdScreen(
    onContinue: () -> Unit,
    onGoAdFree: () -> Unit
) {
    var continueEnabled by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        continueEnabled = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterVertically as Alignment.Horizontal
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Ads help R8 avoid intellectual starvation.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LargeInlineBannerBlock()

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (continueEnabled) onContinue()
            },
            enabled = continueEnabled,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.widthIn(min = 190.dp)
        ) {
            Text(
                text = if (continueEnabled) "Continue" else "${secondsLeft} seconds left",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onGoAdFree,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.widthIn(min = 190.dp)
        ) {
            Text(
                text = "Go ad free",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LargeInlineBannerBlock() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val adWidth = (configuration.screenWidthDp - 40).coerceAtLeast(320)
    val adSize = remember(adWidth) {
        AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth)
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        factory = {
            AdView(it).apply {
                setAdSize(adSize)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}