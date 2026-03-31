package com.inkr8

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.MobileAds
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.data.Users
import com.inkr8.repository.UserRepository

class MainActivity : ComponentActivity() {

    private lateinit var googleLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private val userRepository = UserRepository()
    private var currentUserState = mutableStateOf<Users?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthManager.initGoogle(this)
        MobileAds.initialize(this)
        AdManager.loadAd(this)

        googleLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                AuthManager.linkWithGoogle(
                    result.data,
                    onSuccess = { firebaseUser ->

                        userRepository.updateEmail(
                            userId = firebaseUser.uid,
                            email = firebaseUser.email
                        )

                        userRepository.getUserById(firebaseUser.uid) { user ->
                            currentUserState.value = user
                        }
                    },
                    onError = { it.printStackTrace() }
                )
            }
        }

        setContent {
            var currentUser by currentUserState
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val firebaseUser = AuthManager.currentUser()

                if (firebaseUser != null) {

                    userRepository.updateEmail(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email
                    )

                    userRepository.ensureUserExists(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "User${firebaseUser.uid.take(4)}",
                        email = firebaseUser.email
                    ) { user -> currentUser = user }

                } else {
                    AuthManager.signInAnonymously(
                        onSuccess = { firebaseUserAnon ->
                            userRepository.ensureUserExists(
                                uid = firebaseUserAnon.uid,
                                name = "User${firebaseUserAnon.uid.take(4)}",
                                email = null
                            ) { user -> currentUser = user }
                        },
                        onError = { it.printStackTrace() }
                    )
                }
            }

            if (currentUser == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Signing in...")
                }
                return@setContent
            }

            Inkr8Theme {
                AppRoot(
                    initialUser = currentUser!!,
                    googleLauncher = googleLauncher
                )
            }
        }
    }
}
