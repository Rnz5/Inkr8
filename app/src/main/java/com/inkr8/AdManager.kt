package com.inkr8

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {

    private var interstitialAd: InterstitialAd? = null

    fun loadAd(context: Context) {
        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712", // i dont want to get banned
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showAd(activity: Activity) {
        interstitialAd?.show(activity)
        interstitialAd = null
    }
}