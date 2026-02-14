package com.inkr8

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*

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
                .addOnSuccessListener {
                    onSuccess(it.user!!)
                }
                .addOnFailureListener { onError(it) }

        } catch (e: Exception) {
            onError(e)
        }
    }

    fun signInAnonymously(
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInAnonymously()
            .addOnSuccessListener { onSuccess(it.user!!) }
            .addOnFailureListener { onError(it) }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    fun signOut() {
        googleSignInClient.signOut()
        auth.signOut()
    }

    fun linkWithGoogle(
        intent: Intent?,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)

        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null && currentUser.isAnonymous) {

                currentUser.linkWithCredential(credential)
                    .addOnSuccessListener { result ->
                        onSuccess(result.user!!)
                    }
                    .addOnFailureListener { onError(it) }

            } else {
                onError(Exception("User is not anonymous"))
            }

        } catch (e: Exception) {
            onError(e)
        }
    }

}
