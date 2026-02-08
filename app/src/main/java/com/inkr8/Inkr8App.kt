package com.inkr8

import android.app.Application
import com.google.firebase.FirebaseApp

class Inkr8App : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}