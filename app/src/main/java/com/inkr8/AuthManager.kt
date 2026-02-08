package com.inkr8

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    val auth = FirebaseAuth.getInstance()

    fun ensureSignedIn(onReady: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onReady(currentUser.uid)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    onReady(it.user!!.uid)
                }
        }
    }
}