package com.inkr8

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseUser
import com.inkr8.data.Users
import com.inkr8.repository.UserRepository
import com.inkr8.screens.LoginScreen
import com.inkr8.screens.UsernameSetupScreen
import com.inkr8.ui.theme.Inkr8Theme

class MainActivity : ComponentActivity() {

    private lateinit var googleLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private val userRepository = UserRepository()
    private var currentUserState = mutableStateOf<Users?>(null)
    private var isCheckingAuthState = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthManager.initGoogle(this)
        MobileAds.initialize(this)
        AdManager.loadAd(this)

        googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                AuthManager.firebaseAuthWithGoogle(
                    data = result.data,
                    onSuccess = { firebaseUser ->
                        loadOrCreateUser(firebaseUser)
                    },
                    onError = { it.printStackTrace() }
                )
            }
        }

        setContent {
            Inkr8Theme {
                var currentUser by currentUserState
                var isCheckingAuth by isCheckingAuthState
                var usernameError by remember { mutableStateOf<String?>(null) }
                var isSavingUsername by remember { mutableStateOf(false) }
                var isSigningIn by remember { mutableStateOf(false) }


                LaunchedEffect(Unit) {
                    val firebaseUser = AuthManager.currentUser()

                    if (firebaseUser != null) {
                        loadOrCreateUser(firebaseUser)
                    } else {
                        isCheckingAuth = false
                    }
                }

                when {
                    isCheckingAuth -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading...")
                        }
                    }

                    currentUser == null -> {
                        LoginScreen(
                            onGoogleLogin = {
                                isSigningIn = true
                                val signInIntent = AuthManager.getGoogleSignInIntent()
                                googleLauncher.launch(signInIntent)
                            }
                        )
                    }

                    currentUser != null && !currentUser!!.hasChosenUsername -> {
                        UsernameSetupScreen(
                            isSaving = isSavingUsername,
                            errorMessage = usernameError,
                            onSubmit = { chosenName ->
                                usernameError = null
                                isSavingUsername = true

                                userRepository.claimUsername(
                                    userId = currentUser!!.id,
                                    username = chosenName,
                                    onSuccess = {
                                        userRepository.getUserById(currentUser!!.id) { updatedUser ->
                                            currentUserState.value = updatedUser
                                            isSavingUsername = false
                                        }
                                    },
                                    onError = { e ->
                                        usernameError = e.message
                                        isSavingUsername = false
                                    }
                                )
                            },
                            checkAvailability = { name, callback ->
                                userRepository.isUsernameAvailable(name, callback)
                            },

                            validateUsername = { name ->
                                userRepository.validateUsername(name)
                            }
                        )
                    }

                    else -> {
                        AppRoot(
                            initialUser = currentUser!!,
                            googleLauncher = googleLauncher
                        )
                    }
                }
            }
        }
    }

    private fun loadOrCreateUser(firebaseUser: FirebaseUser) {
        userRepository.updateEmail(
            userId = firebaseUser.uid,
            email = firebaseUser.email
        )

        userRepository.ensureUserExists(
            uid = firebaseUser.uid,
            email = firebaseUser.email
        ) { user ->
            currentUserState.value = user
            isCheckingAuthState.value = false
        }
    }
}