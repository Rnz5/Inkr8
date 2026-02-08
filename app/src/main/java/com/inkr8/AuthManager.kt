package com.inkr8

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun ensureSignedIn(onReady: (FirebaseUser) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onReady(currentUser)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    onReady(result.user!!)
                }
        }
    }
}

