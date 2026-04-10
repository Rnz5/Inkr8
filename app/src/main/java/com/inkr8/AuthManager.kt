package com.inkr8

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

object AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    fun initGoogle(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun firebaseAuthWithGoogle(
        data: Intent?,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onError(Exception("Google sign-in succeeded but user is null."))
                    }
                }
                .addOnFailureListener { onError(it) }

        } catch (e: Exception) {
            onError(e)
        }
    }

    fun linkWithGoogle(
        intent: Intent?,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)

        try {
            val account = try {
                task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                onError(e)
                return
            }
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val currentUser = auth.currentUser

            if (currentUser != null && currentUser.isAnonymous) {
                currentUser.linkWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onError(Exception("Linking succeeded but user is null."))
                        }
                    }
                    .addOnFailureListener { onError(it) }
            } else {
                onError(Exception("Current user is not anonymous."))
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    fun signOut() {
        if (::googleSignInClient.isInitialized) {
            googleSignInClient.signOut()
        }
        auth.signOut()
    }
}